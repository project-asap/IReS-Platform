import logging
import os
import warnings
from deployer.utils import get_random_file_name

with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    import paramiko
    from paramiko.client import SSHClient

import socket
import time
from deployer.conf import SLEEP_TIMEOUT, MAX_WAIT_FOR_LOOPS
from deployer.errors import ArgumentsError
from deployer.connectors import AbstractConnector

__author__ = 'Giannis Giannakopoulos'


class VM:
    """
    Base VM class. This class implements the minimum actions available
    to a single VM
    """
    def __init__(self):
        self.cloud_connector = AbstractConnector()
        self.flavor_id = ''
        self.image_id = ''
        self.name = ''
        self.login_user = ''
        self.login_password = ''
        self.id = ''
        self.hostname = ''

    def create(self):
        """
        Creates a VM, with a specific name for a given flavor and image.
        :raise ArgumentsError: if there exist missing arguments
        """
        logging.getLogger("vm").debug(self.name+":creating VM")
        if self.name == '' or self.image_id == '' or self.flavor_id == '':
            raise ArgumentsError("name, image_id and flavor_id must be declared to start a VM!")
        response = self.cloud_connector.create_vm(name=self.name, image_id=self.image_id, flavor_id=self.flavor_id)
        self.login_user = response['user']
        self.login_password = response['password']
        self.id = response['id']
        self.hostname = response['hostname']

    def delete(self):
        """
        Forcefully destroy the VM .
        """
        logging.getLogger("vm").debug(self.name+":deleting VM")
        self.cloud_connector.delete_vm(self.id)

    def run_command(self, command):
        """
        Execute an ssh command. It opens a new ssh connection.
        """
        if len(command) < 100:
            logging.getLogger("vm").debug(self.name+": running command "+command)
        else:
            logging.getLogger("vm").debug(self.name+": running command (too large to show)")
        sshclient = self.__create_ssh_client()
        stdin_c, stdout_c, stderr_c = sshclient.exec_command(command)
        output = stdout_c.read()
        error = stderr_c.read()
        sshclient.close()
        if output is not None and output != '':
            logging.getLogger("vm").debug(self.name+": stdout:" + output)
        if error is not None and error != '':
            logging.getLogger("vm").debug(self.name+": stderr:" + error)
        return output, error

    def run_script(self, script):
        """
        Transfer a script from the local fs and execute it to the VM.
        """
        sshclient = self.__create_ssh_client()
        sftp = sshclient.open_sftp()
        sftp.put(script, '/tmp/script-to-run')
        sftp.close()
        sshclient.exec_command("chmod +x /tmp/script-to-run")
        stdin_c, stdout_c, stderr_c = sshclient.exec_command("/tmp/script-to-run")
        sshclient.exec_command("rm /tmp/script-to-run")
        sshclient.close()
        output = stdout_c.read()
        error = stderr_c.read()
        return output, error

    def wait_until_visible(self):
        """
        This method blocks until a new SSH connections is established.
        The timeouts and number of tries are defined from configuration files.
        """
        self.__create_ssh_client().close()
        logging.getLogger("vm").debug(self.name+": VM is visible ("+self.hostname+")")
        return

    def get_addresses(self, ip_version=None, connection_type=None):
        return self.cloud_connector.get_server_addresses(self.id,
                                                         ip_version=ip_version,
                                                         connection_type=connection_type)

    def __create_ssh_client(self):
        sshclient = SSHClient()
        sshclient.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        for i in range(1, MAX_WAIT_FOR_LOOPS):
            try:
                sshclient.connect(hostname=self.hostname, port=22,
                                  username=self.login_user, password=self.login_password, timeout=SLEEP_TIMEOUT)
                return sshclient
            except socket.error:    # no route to host is expected here at first
                time.sleep(SLEEP_TIMEOUT)

    def get_file_content(self, remote_path):
        """
        Secure copy file from remote_path and get its contents.
        """
        local_path = "/tmp/"+get_random_file_name()
        sshclient = self.__create_ssh_client()
        sftp = sshclient.open_sftp()
        sftp.get(remote_path, local_path)
        sftp.close()
        f = open(local_path)
        content = f.read()
        f.close()
        os.remove(local_path)
        return content

    def put_file_content(self, file_content, remote_path):
        """
        Secure copy file contents to remote_path
        """
        local_path = "/tmp/"+get_random_file_name()
        sshclient = self.__create_ssh_client()
        sftp = sshclient.open_sftp()
        f = open(local_path, mode='w')
        f.write(file_content)
        f.flush()
        f.close()
        sftp.put(localpath=local_path, remotepath=remote_path)
        sftp.close()
        os.remove(local_path)

    def put_file(self, localpath, remotepath):
        logging.getLogger("vm").debug(self.name+": putting file "+localpath+" to "+self.name+":"+remotepath)
        sshclient = self.__create_ssh_client()
        sftp = sshclient.open_sftp()
        sftp.put(localpath=localpath, remotepath=remotepath)
        sftp.close()

    def update_hosts(self, hosts):
        """
        This method fetches the /etc/hosts file from the VM and
        it updates according to the hosts dictionary. The key in
        hosts is the IP and the value is the domain name.
        """
        logging.getLogger("vm").debug(self.name+": updating hosts")
        old = self.get_file_content("/etc/hosts")
        old += "\n\n"
        old += "# Lines added automatically\n"
        for key, value in hosts.iteritems():
            old += key+"\t"+value+"\n"
        self.put_file_content(file_content=old, remote_path="/etc/hosts")

    def serialize(self):
        d = self.__dict__.copy()
        d.pop("cloud_connector")
        return d

    def deserialize(self, state, cloud_connector):
        for key, value in state.iteritems():
            setattr(self, key, value)
        self.cloud_connector = cloud_connector

    def set_hostname(self):
        logging.getLogger("vm").debug(self.name+": setting hostname")
        self.run_command("hostname "+self.name)

