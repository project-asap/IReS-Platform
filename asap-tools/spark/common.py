#!/usr/bin/env pyspark
__author__ = 'cmantas'
from os import popen

from os import environ



# core_site = environ['HADOOP_HOME']+"/etc/hadoop/core-site.xml"
#
# with open(core_site) as f:
#     content = f.readlines()
#
# hdfs_master = "localhost"
#
# # find the HDFS master from the config file
# for line in content:
#     if "hdfs" not in line.lower():
#         continue

# recover hdfs master from hadoop command
line = popen("hadoop org.apache.hadoop.conf.Configuration | grep fs.defaultFS").read()
host_start = line.index("://")+3
line = line[host_start:]
host_end = line.index(":")
hdfs_master= line[:host_end]

def to_hdfs_url(fname):
    fname = "hdfs://%s:9000/" % hdfs_master + fname
    return fname
