#!/usr/bin/env python
from cement.core import foundation, controller

from lib import get_backend, set_backend
from monitor import *
from pprint import  PrettyPrinter

pprint = PrettyPrinter(indent=2).pprint

backend = get_backend()


def my_split(p):
    """
    splits args based on '=' delim
    :return:
    """
    if p is None: return {}
    delim = '='
    def mini_split(t):
        splitted = t.split(delim)
        if len(splitted)<2:
            raise Exception("could not split '{0}' based on '{1}'".format(t, delim))
        return splitted

    return dict(map(mini_split, p))


# define an application base controller
class MyAppBaseController(controller.CementBaseController):
    class Meta:
        label = 'base'
        description = "My Application does amazing things!"

        # the arguments recieved from command line
        arguments = [
            (['-r', '--retrieve-monitoring'], dict(action='store_true', help='retrieve the monitroing metrics from their temp file')),
            (['-m', '--metrics'], dict(action='store', help='the metrics to report', nargs='*')),
            (['-b', '--backend'], dict(action='store', help='the backend configuration parameters', nargs='*')),
            (['-e', '--experiment-name'], dict(action='store', help='the name of the reported experiment')),
            (['-q', '--query'], dict(action='store', help='the query to execute in the backend storage system')),
            (['-pp', '--plot-params'], dict(action='store', help='parameters of the plot', nargs='*')),
            (['-dict',], dict(action='store_true', help='get the query result in a dict')),
            (['-cm', '--collect-metrics'], dict(action='store_true', help='collect the metrics of an active monitoring process')),
            (['-cs', '--collect-streaming-metrics'], dict(action='store_true', help='collect the metrics of an finished streaming experiment'))
            ]

    @controller.expose(hide=True, aliases=['run'])
    def default(self):
        self.app.log.error('You need to choose one of the options, or -h for help.')

    @controller.expose(help='show examples of execution')
    def show_examples(self):
        print \
        """
        # set a sqlite reporting backend with a specific sqlite file
        ./reporter_cli.py set-backend -b backend=sqlitebackend file=my_database.db
        # Report, for experiment 'my_experiment', some metrics and their values
        ./reporter_cli.py report -e my_experiment -m metric1=test metric2=2
        # plot a timeline of metric 'my_metric'
         ./reporter_cli.py plot-query -q  "select cast(strftime('%s',date) as long) , my_metric from my_table;" -pp xlabel=bull title='my title'
         """

    @controller.expose(aliases=['set-backend'])
    def set_reporting_backend(self):
        self.app.log.info("Setting reporting back-end")
        if self.app.pargs.backend:
            conf =  my_split(self.app.pargs.backend)
            set_backend(conf)
        else:
            self.app.log.error('No backend conf specified')

    @controller.expose()
    def show_backend(self):
        self.app.log.info("Showing reporting back-end")
        print backend

    @controller.expose(help="store the required params", aliases=['r'])
    def report(self):
        experiment = self.app.pargs.experiment_name
        if not experiment:
            self.app.log.error("No experiment name provided. Please use the -e/--experiment-name parameter ")
            exit()

        metrics ={}

        cli_metrics = my_split(self.app.pargs.metrics) # metrics from cmd args
         # metrics stored into a file in the past
        file_metrics = collect_future_metrics()

        streaming_metrics = ganglia_metrics = {}

        if self.app.pargs.collect_streaming_metrics:
            # wait for and collect the streaming metrics if required
            streaming_metrics = collect_streaming_metrics()

        if self.app.pargs.collect_metrics:
            # collect ganglia monitoring metrics if required
            ganglia_metrics = collect_ganglia_metrics()

        # update the metrics variable so that common common entries (if any) follow the priority
        # 1)cli 2)future file 3)streaming 4)ganglia
        metrics.update(ganglia_metrics)
        metrics.update(streaming_metrics)
        metrics.update(file_metrics)
        metrics.update(cli_metrics)

        # report the metrics to the backend
        backend.report_dict(experiment, metrics)

    @controller.expose(help="execute a query to the backend and prints the results")
    def query(self):
        if self.app.pargs.dict:
            res = backend.dict_query(self.app.pargs.experiment_name, self.app.pargs.query)
            pprint(res)
        else:
            res = backend.query(self.app.pargs.experiment_name, self.app.pargs.query)
            for r in res:
                print r


    @controller.expose(help="store some metrics in a local file so that they can be reported later")
    def future_report(self):
        metrics = my_split(self.app.pargs.metrics)
        store_future_metrics(metrics)



    @controller.expose(help="execute a query to the backend and plot the results")
    def plot_query(self):
        pparams = self.app.pargs.plot_params

        if pparams is not None: pparams = my_split(self.app.pargs.plot_params)
        else: pparams = {}
        backend.plot_query(self.app.pargs.experiment_name, self.app.pargs.query, **pparams)


class MyApp(foundation.CementApp):
    class Meta:
        label = 'reporter'
        base_controller = MyAppBaseController


with MyApp() as app:
    app.run()