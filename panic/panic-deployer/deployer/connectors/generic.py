__author__ = 'Giannis Giannakopoulos'


class AbstractConnector:
    """
    Abstract Connector is the IaaS connectors API. Each connector will override
    this class.
    """

    def __init__(self):
        pass

    def authenticate(self, authentication=None):
        raise NotImplemented

    def configure(self, configuration):
        raise NotImplemented

    def create_vm(self, name, flavor_id, image_id):
        """

        :param name:
        :param flavor_id:
        :param image_id:
        """
        raise NotImplemented

    def prepare(self):
        raise NotImplemented

    def delete_vm(self, vm_id):
        """

        :param vm_id:
        :raise NotImplemented:
        """
        raise NotImplemented

    def list_vms(self):
        """


        :raise NotImplemented:
        """
        raise NotImplemented

    def get_status(self, vm_id):
        """

        :param vm_id:
        :raise NotImplemented:
        """
        raise NotImplemented

    def get_server_addresses(self, vm_id):
        raise NotImplemented

    def clone(self):
        raise NotImplemented

    def serialize(self):
        raise NotImplemented

    def deserialize(self, state):
        raise NotImplemented

    def cleanup(self):
        raise NotImplemented