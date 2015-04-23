from kamaki.clients import ClientError
from kamaki.clients.astakos import AstakosClient
from kamaki.clients.cyclades import CycladesClient, CycladesNetworkClient
from sys import stderr
from time import sleep
from deployer.conf import SLEEP_TIMEOUT, MAX_WAIT_FOR_LOOPS
from deployer.connectors.generic import AbstractConnector

__author__ = 'Giannis Giannakopoulos'


class OkeanosConnector(AbstractConnector):
    """
    Okeanos connector.
    """

    def __init__(self):
        AbstractConnector.__init__(self)
        self.__cyclades = None
        self.__network_client = None
        self.attach_public_ipv4 = False
        self.private_network = -1

    def authenticate(self, authentication=None):
        """

        :param authentication:
        :return:
        """
        if self.__cyclades is not None:
            return True
        try:
            authcl = AstakosClient(authentication['URL'], authentication['TOKEN'])
            authcl.authenticate()
            self.__cyclades = CycladesClient(authcl.get_service_endpoints('compute')['publicURL'],
                                             authentication['TOKEN'])
            self.__network_client = CycladesNetworkClient(authcl.get_service_endpoints('network')['publicURL'],
                                                          authentication['TOKEN'])
        except ClientError:
            stderr.write('Connector initialization failed')
            return False
        return True

    def configure(self, configuration):
        self.authenticate(configuration['auth'])
        if 'private_network' in configuration and configuration['private_network']:
            self.private_network = 0
        if 'attach_public_ipv4' in configuration and configuration['attach_public_ipv4']:
            self.attach_public_ipv4 = True

    def prepare(self):
        """
        In this method, application-level IaaS related actions are executed.
        :return:
        """
        if self.private_network == 0:
            self.private_network = self.create_private_network()

    def create_vm(self, name, flavor_id, image_id):
        """

        :param name:
        :param flavor_id:
        :param image_id:
        :return:
        """
        networks = []
        if self.attach_public_ipv4:
            networks.append({'uuid': self.__create_floating_ip()})
        if self.private_network != -1:
            networks.append({'uuid': self.private_network})

        response = self.__cyclades.create_server(name=name, flavor_id=flavor_id, image_id=image_id, networks=networks)
        ret_value = dict()
        ret_value['password'] = response['adminPass']
        ret_value['id'] = response['id']
        ret_value['user'] = response['metadata']['users']
        ret_value['hostname'] = 'snf-' + str(response['id']) + '.vm.okeanos.grnet.gr'
        self.__cyclades.wait_server(server_id=ret_value['id'], current_status='ACTIVE')
        return ret_value

    def delete_vm(self, server_id):
        """
        Delete VM method. The method is blocking until the VM goes to a "DELETED" state
        :param server_id:
        :return:
        """
        attachments = self.__cyclades.get_server_details(server_id)['attachments']
        port_id = None
        for a in attachments:
            if a['OS-EXT-IPS:type'] == 'floating':
                port_id = a['id']
        floating_ip_id = None
        for ip in self.__network_client.list_floatingips():
            if port_id is not None and ip['port_id'] == str(port_id):
                floating_ip_id = ip['id']
        self.__cyclades.delete_server(server_id)
        self.__cyclades.wait_server(server_id, current_status='DELETED')    # wait until server is deleted
        if floating_ip_id is not None:
            self.__wait_until_ip_released(floating_ip_id)
            self.__network_client.delete_floatingip(floating_ip_id)

    def __wait_until_ip_released(self, floating_ip_id):
        for i in range(1, MAX_WAIT_FOR_LOOPS+1):
            for ip in self.__network_client.list_floatingips():
                if ip['id'] == floating_ip_id:
                    if ip['instance_id'] is None or ip['instance_id'] == 'None':
                        return True
            sleep(SLEEP_TIMEOUT)

    def list_vms(self):
        """


        :return:
        """
        return self.__cyclades.list_servers()

    def get_status(self, vm_id):
        """

        :param vm_id:
        :return:
        """
        return self.__cyclades.get_server_details(vm_id)

    def get_server_addresses(self, vm_id, ip_version=None, connection_type=None):
        """
        Returns the enabled addresses, as referenced from the IaaS.
        """
        addresses = self.__cyclades.get_server_details(vm_id)['addresses']
        results = []
        while len(addresses) > 0:
            key, value = addresses.popitem()
            if (ip_version is None or value[0]['version'] == ip_version) and \
                    (connection_type is None or value[0]['OS-EXT-IPS:type'] == connection_type):
                results.append(value[0]['addr'])
        return results

    def __create_floating_ip(self):
        self.__network_client.floatingips_get()
        response = self.__network_client.create_floatingip()
        return response['floating_network_id']

    def create_private_network(self):
        """
        Creates a new private network and returns its id
        """
        response = self.__network_client.create_network(type='MAC_FILTERED', name='Deployment network')
        self.__network_client.create_subnet(
            network_id=response['id'],
            enable_dhcp=True,
            cidr='192.168.0.0/24'
        )
        return response['id']

    def clone(self):
        new_connector = OkeanosConnector()
        new_connector.attach_public_ipv4 = self.attach_public_ipv4
        new_connector.private_network = self.private_network
        new_connector.__network_client = self.__network_client
        new_connector.__cyclades = self.__cyclades
        return new_connector

    def cleanup(self):
        if self.private_network != -1 and self.private_network != 0:
            self.__wait_until_private_net_is_empty(self.private_network)
            self.__network_client.delete_network(self.private_network)

    def __wait_until_private_net_is_empty(self, private_net_id):

        for i in range(1, MAX_WAIT_FOR_LOOPS):
            port_set = set()
            for p in self.__network_client.list_ports():
                port_set.add(p['network_id'])
            if private_net_id not in port_set:
                return
            else:
                sleep(SLEEP_TIMEOUT)

    def serialize(self):
        d = dict()
        d['attach_public_ipv4'] = self.attach_public_ipv4
        d['private_network'] = self.private_network
        return d

    def deserialize(self, state):
        self.attach_public_ipv4 = state['attach_public_ipv4']
        self.private_network = state['private_network']