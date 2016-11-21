__author__ = 'cmantas'
import matplotlib.pyplot as plt
from ast import literal_eval
from time import time, sleep
from json import load, dumps
from os.path import isfile

try:
    plt.style.use('fivethirtyeight')
except:
    # print "You could get prettier graphs with matplotlib > 1.4"
    pass


from matplotlib.pyplot import figure, show


def myplot(*args, **kwargs):
    if "title" in kwargs:
        title = kwargs["title"]
        del(kwargs["title"])
        plt.title(title)
    if "xlabel" in kwargs:
        xlabel = kwargs["xlabel"]
        del(kwargs["xlabel"])
        plt.xlabel(xlabel)
    if "ylabel" in kwargs:
        ylabel = kwargs["ylabel"]
        del(kwargs["ylabel"])
        plt.ylabel(ylabel)

    plt.grid(True)
    # plt.grid(which='both')
    # plt.grid(which='minor', alpha=0.2)
    plt.plot(*args, **kwargs)
    plt.legend(loc = 'upper left')


def mycast(a):
    """
    given a string, it returns its casted value to the correct type or the string itself if it can't be evaluated
    if the input is a list or a dict it recursively calls itself on the input collection's (keys and) values
    :param a: the input string
    :return: the evaluated 'casted' result
    """
    if isinstance(a, dict):
        return dict(map(lambda (k, v): (mycast(k),mycast(v)), a.iteritems()))
    elif isinstance(a, list):
        return map(mycast, a)
    else:
        try:
            return literal_eval(a)
        except:
            return a


def wait_for_file(filepath, timeout):
    """ Keep waiting for a file to appear unless a timeout is reached
    :param filepath:
    :param timeout: the time needed to give up (default: 3sec)
    :return: void
    """
    end_time= time() + timeout
    #wait
    while not isfile(filepath) and time()<end_time:
        sleep(0.2)
    # if after wait no file then trouble
    if not isfile(filepath):
        print "ERROR: waited for monitoring data file, but timed out"
        exit()


def _collect_json(metrics_file, timeout=3):
    try:
        # wait for the metrics file to be created (timeout secs)
        if timeout: wait_for_file(metrics_file, timeout)

        # collect the saved metrics from metrics file
        with open(metrics_file) as f:
            metrics = load(f)
            return metrics
    except:
        #print 'Could not collect the metrics'
        return {}
