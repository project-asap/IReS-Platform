__author__ = 'cmantas'


class ConsoleBackend(object):
    """
    A simple Console backend that can only report to stdout
    """

    def report(self, experiment_name, **kwargs):
        print "reporting to console"
        print experiment_name + ": ",
        for k, v in kwargs.iteritems():
            print k,'=',v," ",
        print ""

    def report_dict(self, experiment_name, in_dict):
        self.report(experiment_name, **in_dict)

    def __str__(self):
        return str(self.__class__.__name__)