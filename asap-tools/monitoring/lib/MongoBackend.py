__author__ = 'cmantas'
import datetime
from ConsoleBackend import ConsoleBackend
from lib.tools import myplot, show, mycast
from pymongo import MongoClient





def cast_dict(a):
    """
    given a dictionary of string-->string, it creates another dictionary with the same keys and the casted values of
    the original as its values values
    :param a: a dictionary of string-->string
    :return:
    """
    return dict(map(lambda (k, v): (k,mycast(v)), a.iteritems()))


class MongoBackend(ConsoleBackend):
    def __init__(self, host='localhost', port=27017):

        # set defaults again
        if host is None:
            self.host='localhost'
        else:
            self.host=host
        if port is None:
            self.port=27017
        else:
            self.port = int(port)

        self.client = MongoClient(self.host, self.port)
        # using the metrics db
        self.db = self.client.metrics

    def _get_collection(self, collection):
        return eval("self.db.{0}".format(collection))

    def query(self, experiment, query, sort=None, tupples=True):
        dict_result = self.dict_query(experiment, query, sort)
        return map(lambda d:d.values(), dict_result)

    @staticmethod
    def parse_query(query):
        # eval query if it is string
        if type(query) is str: q = eval(query)
        else: q = query

        if type(q) is tuple:
            selection = q[0]
            projection = q[1]
        elif type(q) is dict:
            selection = q
            projection = None
        else:
            raise Exception("I cannot handle that kind of query: "+str(type(q)))
        return  selection,projection

    def dict_query(self, experiment, query, sort=None):
        selection, projection = self.parse_query(query)
        return self.find(experiment, selection, projection, sort=sort)

    def report(self, experiment_name, **kwargs):
        # cast the dict values into their respective types
        casted = cast_dict(kwargs)
        # using the experiment name as a collection
        metrics = eval("self.db.{0}".format(experiment_name))
        r = metrics.insert_one(casted)

    def find(self, experiment, selection={}, projection=None, tuples=True, sort=None):
        collection = self._get_collection(experiment)
        if sort is None:
            rows = collection.find(selection, projection)
        else:
            rows = collection.find(selection, projection).sort(sort)
        if tuples: return tuple(rows)
        else: return rows

    def aggregate(self, experiment, aggregation, tuples=True):
        collection = self._get_collection(experiment)
        rows = collection.aggregate(aggregation)
        if tuples: return tuple(rows)
        else: return rows
        return rows


    def __str__(self):
        return "MongoBackend({0},{1})".format(self.host, self.port)

    def plot_query(self, experiment, query, sort=None, show_plot=True, **kwargs):
        # make sure that _id column is not returned
        selection, projection = self.parse_query(query)
        projection['_id'] = 0
        rows = self.query(experiment, (selection,projection), sort)

        # transpose the rows
        rows_transposed = zip(*rows)

        # plot the result
        myplot(*rows_transposed, **kwargs)

        # show the plot
        if show_plot:show()


    def plot_aggregation(self, experiment, query, show_plot=True, **kwargs):
        # make sure that _id column is not returned
        # selection, projection = self.parse_query(query)
        # projection['_id'] = 0

        rows = self.aggregate(experiment, query)
        # rows from dict to tuple
        rows = map(lambda d:d.values(), rows)
        # transpose the rows
        rows_transposed = zip(*rows)

        # plot the result
        myplot(*rows_transposed, **kwargs)

        # show the plot
        if show_plot:
            show()
        return rows_transposed



