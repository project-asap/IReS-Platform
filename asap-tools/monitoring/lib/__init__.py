__author__ = 'cmantas'
from ConsoleBackend import ConsoleBackend
from SQLiteBackend import SQLiteBackend
from MongoBackend import MongoBackend
from os.path import abspath
REPORT_CONFIG_FILE='/etc/reporter_config.json'
from os.path import isfile
from json import dump, load
from ast import literal_eval


global reporter_backend
reporter_backend = None


def set_backend(config):
    """
    Saves the backend configuration to the conf file
    :param config:
    :return:
    """
    if 'file' in config:
        config['file']=abspath(config['file'])
    print "Setting Backend" + str(config)
    f = open(REPORT_CONFIG_FILE, "w+")
    dump(config, f, indent=3)


def get_backend():
    if reporter_backend is None:
        load_backend_conf()
    return reporter_backend


def load_backend_conf():
    """
    Retrieves the backend configuration from the conf file (or defaults to console
    :return:
    """
    global reporter_backend

    # if there is no config file choose console backend
    if not isfile(REPORT_CONFIG_FILE):
        print "Defaulting to console because no conf file"
        reporter_backend = ConsoleBackend()
        return
    # load the config file and choose the backend specified
    conf = load(open(REPORT_CONFIG_FILE))
    backend_name =conf['backend']
    #print backend_name
    if backend_name.lower()=='sqlite' or backend_name.lower()=='sqlitebackend':
        file = conf['file']
        reporter_backend = SQLiteBackend(file)
    elif backend_name.lower()=='console' or backend_name.lower()=='consolebackend':
        reporter_backend = ConsoleBackend()
    elif backend_name.lower().startswith("mongo"):
        port =conf.get('port')
        host = conf.get('host')
        reporter_backend = MongoBackend(host, port)
    else:
        print 'shit i could not find backend: '+ backend_name


def report(*args, **kwargs):
    """ reports the values of an experiment """
    if reporter_backend is None:
        load_backend_conf()
    reporter_backend.report(*args, **kwargs)


def report_dict(*args, **kwargs):
    """ reports the values of an experiment taken as a dict"""
    if reporter_backend is None:
        load_backend_conf()
    reporter_backend.report_dict(*args, **kwargs)


# a simple consore reporting function
simple_report = ConsoleBackend().report

# load conf everytime something is imported
# load_backend_conf()
