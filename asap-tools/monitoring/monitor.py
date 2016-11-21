#!/usr/bin/env python
from time import sleep, time
import sys
from signal import signal, SIGTERM, SIGALRM
from json import dumps
from os import getpid, kill, remove
from shutil import move
from pprint import pprint
from lib.GMonitor import GMonitor
from lib.tools import _collect_json

__author__ = 'cmantas'

# ---------- some global vars ---------------
metrics_file = 'asap_monitoring_metrics.json'  # default metrics file
interval = 5  # default sampling interval
# absolute max monitoring time (as a fail-safe timeout)
max_monitoring_time = 3*60*60  # 3 hours

_PID_FILE = 'asap_monitoring.pid'
_STATS_FILE = "spark_streaming_experiment.json"
_FUTURE_FILE = "future_metrics.json"


# the monitor instance
monitor = None


def print_out(*sigargs):
    """
    This prints out the
    this will be called in case of Ctrl-C or kill signal
    :return:
    """
    end_time = time()
    time_delta = end_time - start_time

    global monitor

    output = {'metrics_timeline': monitor.metrics_timeline,
              'metric_summaries_timeline': monitor.metrics_summaries_timeline,
              'time': time_delta,
              "iops_rd_total": monitor.iops_rd_total,
              "iops_wt_total": monitor.iops_wt_total,
              "net_out_total": monitor.net_out_total,
              "net_in_total": monitor.net_in_total
              }

    if metrics_file is not None:
        # remove the old metrics file immediately
        try:
            remove(metrics_file)
        except:
            pass

        # open a temp file and write the metrics there
        tmp_file = '/tmp/asap_metrics_temp'
        with open(tmp_file, "w+") as f:
            f.write(dumps(output, indent=1))

        # after write is finished move the tmp file to the output file
        move(tmp_file, metrics_file)

        # print 'written on ', metrics_file, '\n'
    else:
        # console output
        print dumps(output, indent=1)
        sys.stdout.flush()
    # we are done, exit the whole script
    exit()


def send_kill():
    """ Send the kill signal to a previously running instance """
    # read the pid from the pid file
    try:
        with open(_PID_FILE) as f:
            monitor_pid = int(f.read())
            # send the stop signal to the active monitoring process
            kill(monitor_pid, SIGTERM)
        remove(_PID_FILE)
    except:
        pass


def collect_ganglia_metrics():
    """
    Read metrics from a json metrics file created by another instance
    :return: a dict of metrics
    """
    # send sigterm in case there is another live monitoring process
    send_kill()
    # wait for file and collect the metrics
    global metrics_file
    return _collect_json(metrics_file)


def collect_streaming_metrics():
    """
    This will wait for the streaming metrics file to be created and get it
    Assumes that the streaming metrics file *HAS NOT* yet been created by the
     time the function is called
    :return:
    """
    try:
        remove(_STATS_FILE)
    except:
        pass

    return _collect_json(_STATS_FILE, timeout=999999)


def collect_future_metrics():
        """
        Loads metrics that were previously stored in a json file if any.
         If not, it returns an empty dict
        :return:
        """
        rv = _collect_json(_FUTURE_FILE, timeout=None)
        try:
            remove(_FUTURE_FILE)
        except:
            pass
        return rv


def store_future_metrics(metrics):
    """
    store the metrics in a json file so that they will be collected
    in the future
    :param metrics:
    :return:
    """
    with open(_FUTURE_FILE, "w+") as f:
        f.write(dumps(metrics, indent=1))


if __name__ == "__main__":

    # ############## args parsing ################ #
    from argparse import ArgumentParser
    parser = ArgumentParser(description='Monitoring')
    parser.add_argument("-f", '--file', help="the output file to use")
    parser.add_argument("-c", '--console',
                        help="output the metrics in console",
                        dest='console', action='store_true')
    parser.set_defaults(console=False)
    parser.add_argument("-eh", '--endpoint-host',
                        help="the ganglia endpoing hostname or IP",
                        default="master")
    parser.add_argument("-ep", '--endpoint-port',
                        help="the ganglia endpoing port",
                        type=int, default=8649)
    parser.add_argument("-cm", '--collect-metrics', help="collect the metrics",
                        action='store_true')
    parser.add_argument("-cs", '--collect-streaming-metrics',
                        help="wait and collect the metrics of a streaming\
                         experiment", action='store_true')
    parser.add_argument('--summary', help="only keep a summary of metrics",
                        action='store_true')
    parser.add_argument('-mh', '--monitor-hosts',
                        help='The hosts to monitor \
                        (if not defined, monitor all)', default=None)
    args = parser.parse_args()
# ############################################################ #

    # if we are just collecting gagnlia metrics, then do that and exit
    if args.collect_metrics:
        m = collect_ganglia_metrics()
        if args.console:
            pprint(m)
        exit()

    # if we are just collecting streaming metrics, then do that and exit
    if args.collect_streaming_metrics:
        m = collect_streaming_metrics()
        if args.console:
            pprint(m)
        exit()

    # signal the previous process in case there is one
    send_kill()

    # delete any old metrics files
    try:
        remove(metrics_file)
    except:
        pass

    # chose the output file (or console)
    if args.file is not None:
        metrics_file = args.file
    elif args.console:
        metrics_file = None

    # the ganglia endpoint
    endpoint = (args.endpoint_host, args.endpoint_port)

    # the lists of hosts to monitor (or None for all hosts)
    hosts = args.monitor_hosts.split(',') if args.monitor_hosts else None

    # create the monitor instance
    monitor = GMonitor(endpoint, summarized=args.summary, hosts=hosts)

    # store the pid in the pid file
    with open(_PID_FILE, 'w+') as f:
        f.write(str(getpid()))

    # install the signal handler
    signal(SIGTERM, print_out)

    # start kepping time
    start_time = time()

    # failsafe timeout (in case monitoring is never stopped)
    max_timeout = start_time + max_monitoring_time

    # until signaled or failsafe timeout expired, keep updating the metrics
    try:
        while time() < max_timeout:
            monitor.update_metrics()
            sleep(interval)
    except KeyboardInterrupt:
        print_out()
