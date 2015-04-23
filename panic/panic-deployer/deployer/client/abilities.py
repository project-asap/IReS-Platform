import json
import logging
import os
from deployer import conf
from deployer.components.deployment import Deployment
from deployer.connectors.okeanos import OkeanosConnector

__author__ = 'Giannis Giannakopoulos'


def configure_logger():
    """
    Logging configuration
    :return:
    """
    logging.basicConfig()
    logging.getLogger("root").setLevel(conf.LOG_LEVEL)
    logging.getLogger("vmgroup").setLevel(conf.LOG_LEVEL)
    logging.getLogger("vm").setLevel(conf.LOG_LEVEL)


def transform_description(description, path_prefix):
    """
    This function replaces the script paths with script contents.
    :param description:
    :return:
    """

    logging.getLogger("root").info("Transforming application description")
    groups = description['groups']
    for g in groups:
        scripts = g['scripts']
        for s in scripts:
            if 'path' in s:
                f = open(path_prefix+"/"+s['path'])
                con = f.read()
                f.close()
                s['content'] = con
                s.pop("path", None)
    return description


def parse_description_file(description_file_path):
    """

    :param description_file_path:
    :return: The description in dictionary form
    """
    logging.getLogger("root").info("Parsing application description")
    f = file(description_file_path)
    content_json = f.read()
    f.close()
    content = json.loads(content_json)
    description = transform_description(
        content,
        os.path.dirname(os.path.abspath(description_file_path)))
    return description


def configure_connector(provider):
    """
    Configures a new cloud connector and authenticates the cloud user.
    :param cloud_name:
    :param credentials:
    :return:
    """
    logging.getLogger("root").info("Configuring the cloud connector")
    if provider['name'] == "~okeanos" or provider['name'] == "okeanos":
        connector = OkeanosConnector()
        connector.configure(provider)
        return connector
    else:
        raise NotImplemented("The connector is not supported")


def start_deployment(cloud_connector, description):
    """
    Starting a new deployment
    :param cloud_connector:
    :param description:
    :return:
    """
    logging.getLogger("root").info("Preparing the connector")
    cloud_connector.prepare()
    logging.getLogger("root").info("Starting new deployment")
    deployment = Deployment()
    deployment.cloud_connector = cloud_connector
    deployment.configure(description)
    logging.getLogger("root").info("Launching deployment")
    deployment.launch()
    logging.getLogger("root").info("Executing deployment scripts")
    while deployment.has_more_steps():
        deployment.execute_script()
    return deployment


def terminate_deployment(deployment):
    """
    Terminate deployment ability
    :param deployment:
    :return:
    """
    logging.getLogger("root").info("Terminating deployment")
    deployment.terminate()


def load_state_file(statefile_path):
    """
    This method loads the state file and create a deployment object and a cloud connector.
    :param statefile_path: the path where the statefile exists
    :return: deployment object, cloud connector object
    """
    logging.getLogger("root").info("Loading state file")
    f = open(statefile_path, 'r')
    json_content = f.read()
    f.close()
    state = json.loads(json_content)
    cloud_connector = configure_connector(state['provider'])
    deployment = Deployment()
    deployment.deserialize(state['deployment'], cloud_connector)
    return deployment, cloud_connector


def save_state_file(deployment, description, statefile_path, indent=2):
    """
    Save the statefile of the deployment to the specified path
    :param deployment:
    :param statefile_path:
    :return:
    """
    logging.getLogger("root").info("Saving state file")
    dictionary = dict()
    dictionary['deployment'] = deployment.serialize()
    dictionary['provider'] = description['provider']
    json_content = json.dumps(dictionary, indent=indent)
    f = open(statefile_path, 'w')
    f.write(json_content)
    f.flush()
    f.close()