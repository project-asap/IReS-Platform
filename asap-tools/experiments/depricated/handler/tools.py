#!/usr/bin/env python
__author__ = 'cmantas'


import sqlite3
import matplotlib.pyplot as plt
import argparse
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import matplotlib.pyplot as plt
from matplotlib.pyplot import figure, show
import numpy as np

conn = sqlite3.connect('../results.db')
c = conn.cursor()
# print plt.style.available
plt.style.use('fivethirtyeight')

def query2lists(query):
    print query
    rv = []
    query = query.lower()
    fi = query.find("from")
    si = query.find("select")
    attributes = len(query[si+6:fi].split(","))
    for i in range(attributes):
        rv.append([])

    rows = c.execute(query)
    # if k is None :
    for row in rows:
        for i in range(attributes):
            rv[i].append(row[i])

    return tuple(rv)

def take_single(query):
    rows = c.execute(query)
    for r in rows:
        return r


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

def plot_from_query(query, **kwargs):
    x, y = query2lists(query)
    myplot(x, y, **kwargs)

def multi_graph(table, x, y, cond_list, groupBy="", where="", **kwargs):

    if 'title' not in kwargs:
        kwargs['title'] = x+" vs "+y
    if 'xlabel' not in kwargs:
        kwargs['xlabel'] = x
    if 'ylabel' not in kwargs:
        kwargs['ylabel'] = y
    if groupBy != "":
        groupBy = "group by "+groupBy
    if where !="":
        where = where+" and "



    # for c in cond_list:
    #     query = "select {0} from {1} where {2} {3}".format(x+','+y, table, c, groupBy)
    #     rx, ry = query2lists(query)
    #     myplot(rx,ry, label=c, title=title, xlabel=xlabel, ylabel=ylabel)
    # show()
    query = "select {0} from {1} where {2}".format(x+","+ y, table, where) + "{0} " + groupBy
    multi_graph_query(query, cond_list, **kwargs)

def multi_graph_query(query, cond_list, **kwargs):
    figure()
    for c in cond_list:
        queryf = query.format(c)
        rx, ry = query2lists(queryf)
        myplot(rx,ry, label=c, **kwargs)


def cond_producer(a, list):
    return [a+"={}".format(i) for i in list]


def join_minDF_query_docs(table, tfidf_table, k, minDf, where_extra=""):
    if not where_extra == "":
        where_extra = " and "+where_extra

    query="""
select avg({0}.documents/1000), {0}.time/1000 from
   {0} inner join {1} on
   {1}.documents={0}.documents and {1}.dimensions={0}.dimensions and k={2} and {1}.minDF={3} {4}
   GROUP BY {0}.documents"""
    return query.format(table, tfidf_table, k, minDf, where_extra)


def draw_single_kmeans(engine, k, minDF, where_extra="", hide_minDF=False, **kwargs):
    tfidf_table = engine +"_tfidf"
    kmeans_table = engine +"_kmeans_text"
    query = join_minDF_query_docs(kmeans_table, tfidf_table, k, minDF, where_extra=where_extra)
    if not hide_minDF:
        kwargs["label"] = "{}, k={}, minDF={}".format(engine, k, minDF)
    else:
         kwargs["label"] = "{}, k={}".format(engine, k)
    kwargs["xlabel"] = "documents/1000"
    kwargs["ylabel"] = "time (sec)"
    plot_from_query(query, **kwargs)


def join_query(in_dict, where=""):
    query="""
    select {table}.documents/1000, {table}.time/1000 from
       {table} inner join {tfidf_table} on
       {tfidf_table}.documents={table}.documents and {tfidf_table}.dimensions={table}.dimensions and {tfidf_table}.minDF={minDF} {where}

       """
    in_dict['where']=where
    return query.format(**in_dict)




