__author__ = 'cmantas'
from sqlite3 import connect
from tools import myplot, show
from ConsoleBackend import ConsoleBackend
import traceback

class SQLiteBackend(ConsoleBackend):
    def __init__(self, sql_file):
        self.file=sql_file
        self.connection = connect(self.file)
        self.cursor = self.connection.cursor()
        #aliases
        self.commit= self.connection.commit
        self.execute = self.cursor.execute

    def report(self, experiment_name, **kwargs):

        # make metrics timeline a string
        if "metrics_timeline" in kwargs:
            kwargs['metrics_timeline'] = '"' + str(kwargs['metrics_timeline'] )+'"'
        key_string="( "
        value_string="( "
        for k, v in kwargs.iteritems():
            key_string += str(k) + ","
            value_string+=str(v) + ","
        key_string = key_string[:-1] + ')'
        value_string = value_string[:-1]+')'
        query = 'INSERT INTO '+ experiment_name+key_string + " VALUES "+ value_string
        #print query
        try:
            self.execute(query)
            self.commit()
        except:
            print "Query failed!"
            print "query was: "+query
            print(traceback.format_exc())


    def query(self, experiment, query, tuples=True):

        # ignoring experiment name because it is included in the query
        rows = self.execute(query)
        # return rows as tupple(list) rather than
        if tuples: return tuple(rows)
        else: return rows



    @staticmethod
    def dict_factory(cursor, row):
        d = {}
        for idx, col in enumerate(cursor.description):
            d[col[0]] = row[idx]
        return d

    def plot_query(self, experiment, query, **kwargs):
        rows = self.query(experiment, query)

        # transpose the rows
        rows_transposed = zip(*rows)

        # plot the result
        myplot(*rows_transposed, **kwargs)
        show()
        return rows_transposed

    def dict_query(self, experiment, query):
        # a factory from rows-->dict
        rows =tuple(self.query(experiment, query))
        # r= rows.fetchone()
        # return self.dict_factory(self.cursor, rows[0])
        return map(lambda t:self.dict_factory(self.cursor, t), rows)

    def  __str__(self):
        return super(SQLiteBackend, self).__str__() + "({0})".format(self.file)





