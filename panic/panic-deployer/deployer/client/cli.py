import json
from optparse import OptionParser
from pprint import pprint
import sys
from deployer.client.abilities import configure_logger, parse_description_file, configure_connector, start_deployment, \
    load_state_file, save_state_file, terminate_deployment

__author__ = 'Giannis Giannakopoulos'


def configure_options():
    parser = OptionParser(prog="deployer", version="v0.1", usage=" %prog %v")
    parser.add_option("-d",
                      "--desc",
                      dest='description',
                      type="string",
                      help="The application description json")

    parser.add_option("-s",
                      "--save-state",
                      dest='statefile_save',
                      type='string',
                      help='the file to save the deployment state')

    parser.add_option("-l",
                      "--load-state",
                      dest='statefile_load',
                      type='string',
                      help='the json statefile you want to load')

    parser.add_option("-a",
                      "--actions",
                      dest='action',
                      choices=['launch', 'terminate', 'show'],
                      help="action to do")

    (options, args) = parser.parse_args()
    return (options, parser)


def endpoint():
    (options, parser) = configure_options()
    if options.action is None:
        parser.error("please provide an action")

    configure_logger()

    description = None
    cloud_connector = None

    if options.description is not None:
        description = parse_description_file(description_file_path=options.description)
        cloud_connector = configure_connector(description['provider'])

    if options.action == "launch":
        if description is None or cloud_connector is None:
            sys.stderr("Please provide description file...")
            exit(1)
        deployment = start_deployment(cloud_connector, description)
        if options.statefile_save is not None:
            save_state_file(deployment, description, options.statefile_save)

    if options.action == 'terminate':
        if options.statefile_load is not None:
            deployment, cloud_connector = load_state_file(options.statefile_load)
            terminate_deployment(deployment)
        else:
            sys.stderr("I need a statefile to load the configuration from...")
            exit(1)

    if options.action == 'show':
        if options.statefile_load is not None:
            f = open(options.statefile_load)
            json_content = f.read()
            d = json.loads(json_content)
            for g in d['deployment']['groups']:
                g.pop('scripts')
            pprint(d)

if __name__ == "__main__":
    endpoint()

