import logging
from threading import Thread
from deployer.components.vmgroup import VMGroup
from deployer.errors import ArgumentsError
from deployer.utils import generate_ssh_key_pair, get_random_file_name

__author__ = 'Giannis Giannakopoulos'


class Deployment:
    """
    This class represents a deployment entity. It holds a number of VMGroups and
    it is responsible for the allocation and the orchestration of the cloud resources.
    """

    def __init__(self):
        self.inject_ssh_key_pair = True
        self.update_hosts = True
        self.set_hostnames = True
        self.__vm_groups = list()
        self.cloud_connector = None
        self.name = ''
        self.private_network = -1

    def configure(self, description):
        """
        This method configures new VMgroup objects according to the received description.
        :param description:
        :return:
        """
        if self.cloud_connector is None:
            raise ArgumentsError("Connector must be set!")
        self.name = description['name']
        if 'inject_ssh_keypair' in description['actions']:
            self.inject_ssh_key_pair = description['actions']['inject_ssh_keypair']
        if 'update_etc_hosts' in description['actions']:
            self.update_hosts = description['actions']['update_etc_hosts']
        if 'set_hostnames' in description['actions']:
            self.set_hostnames = description['actions']['set_hostnames']

        for group in description['groups']:
            g = VMGroup()
            g.configure(group)
            g.cloud_connector = self.cloud_connector.clone()
            for ability, value in group['provider_actions'].iteritems():
                setattr(g.cloud_connector, ability, value)
            self.__vm_groups.append(g)

    def launch(self):
        logging.getLogger("deployment").info("Starting deployment")
        self.__spawn_threads('create')
        logging.getLogger("deployment").info("VMs visible -- construcing and injecting key pairs")
        if self.inject_ssh_key_pair:
            keys = generate_ssh_key_pair(keys_prefix=get_random_file_name())
            self.__spawn_threads('inject_ssh_key', args=[keys['private'], keys['public']])

        if self.update_hosts:
            logging.getLogger("deployment").info("Ok -- setting /etc/hosts files")
            hosts = dict()
            for vmg in self.__vm_groups:
                for ip, host in vmg.get_addresses().iteritems():
                    hosts[ip] = host
            for vmg in self.__vm_groups:
                vmg.set_hosts(hosts)

        if self.set_hostnames:
            logging.getLogger("deployment").info("Setting hostnames")
            self.__spawn_threads('set_hostnames')

    def execute_script(self):
        self.__spawn_threads('execute_script')

    def has_more_steps(self):
        for g in self.__vm_groups:
            if g.has_more_scripts():
                return True
        return False

    def terminate(self):
        self.__spawn_threads('delete')
        self.cloud_connector.cleanup()

    def __spawn_threads(self, method_to_call, args=None):
        """
        :param method_to_call:
        :param args:
        """
        threads = []
        for vm in self.__vm_groups:
            if args is None:
                t = Thread(target=getattr(vm, method_to_call))
            else:
                t = Thread(target=getattr(vm, method_to_call), args=args)
            t.start()
            threads.append(t)
        for t in threads:
            t.join()

    def serialize(self):
        d = dict()
        d['name'] = self.name
        d['groups'] = list()
        d['connector'] = self.cloud_connector.serialize()
        for g in self.__vm_groups:
            d['groups'].append(g.serialize())
        return d

    def deserialize(self, state, cloud_connector):
        self.cloud_connector = cloud_connector
        for key, value in state['connector'].iteritems():
            setattr(self.cloud_connector, key, value)
        self.name = state['name']
        for group_state in state['groups']:
            group = VMGroup()
            group.deserialize(group_state, cloud_connector.clone())
            self.__vm_groups.append(group)