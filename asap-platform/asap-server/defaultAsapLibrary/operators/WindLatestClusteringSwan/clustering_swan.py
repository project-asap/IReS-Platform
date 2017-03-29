#
# Copyright 2015-2017 ASAP
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import datetime
import os
import sys

from dateutil import rrule
from utils import quiet_logs

"""Profiles Clustering modules Module

Given a set of user profiles, it returns typical calling behaviors (clusters)
and a label for each behavior (i.e. resident, commuter, etc.)
More specifically, the clustering algorithm used is Swan KMeans and
the cluster labels are computed by the minimum euclidean distance of the cluster center
and a number of labeled characteristic behaviors.

Usage:
    $SPARK_HOME/bin/spark-submit sociometer/clustering.py <profiles dataset> <region> <start_date> <end_date>

Args:
    profile dataset: The profiles dataset prefix. Can be any Hadoop-supported file system URI.
                     The full path dataset name it computed as:
                     <profile dataset>/<region>/<start_date>_end_date>
                     The expected dataset schema is:
                     <region>,<user_id>,<profile>.
                     The <profile> is a 24 element list containing the sum of user calls for each time division.
                     The column index for each division is: <week_idx> * 6 + <is_weekend> * 3 + <timeslot>
                     where <is_weekend> can be 0 or 1 and <timeslot> can be 0, 1, or 2.
    tag: The tag name featuring in the stored results
    start_date: The analysis starting date. Expected input %Y-%m-%d
    end_date: The analysis ending date. Expected input %Y-%m-%d

Results are stored into several hdfs files: /centroids/<region>/<year>_<week_of_year>
where <year> and <week_of_year> are the year and week of year index of the starting week
of the 4 week analysis.

Example:
    $SPARK_HOME/bin/spark-submit \
        sociometer/user_profiling.py hdfs:///profiles/roma \
        roma 2016-01-01 2016-01-31

The results will be sotred in the hdfs files:
/centroids/roma/2015_53
/centroids/roma/2016_01
/centroids/roma/2016_02 etc
"""
ARG_DATE_FORMAT = '%Y-%m-%d'

folder = sys.argv[1]
region = sys.argv[2]
start_date = datetime.datetime.strptime(sys.argv[3], ARG_DATE_FORMAT)
end_date = datetime.datetime.strptime(sys.argv[4], ARG_DATE_FORMAT)

weeks = [d.isocalendar()[:2] for d in rrule.rrule(
    rrule.WEEKLY, dtstart=start_date, until=end_date
)]

for year, week in weeks:
    lfolder = folder.replace('hdfs:///','')
    print '>>>>', folder, '>>>>', lfolder, '>>>>', region, '>>>>', year, '>>>>', week
    rsubfolder = "%s/%s/%s_%s" % (lfolder, region, year, week)
    asubfolder = "/%s/%s/%s_%s" % (lfolder, region, year, week)
    exists = os.system("$HADOOP_PREFIX/bin/hdfs dfs -test -e %s" % asubfolder)
    print '>>>', asubfolder, exists
    if exists != 0:
        print 'Non-Existent file ', asubfolder
        continue
    print '>>>>', 'Exists'
    os.system("mkdir -p %s/%s" % (lfolder, region))
    os.system("$HADOOP_PREFIX/bin/hdfs dfs -getmerge %s %s" % (asubfolder, rsubfolder))
    os.system("mkdir -p centroids/%s" % region)
    # os.environ["PATH"] = os.environ["PATH"] + ":/home/forth/asap4all/qub/bin"
    os.environ["LD_LIBRARY_PATH"] = os.environ["LD_LIBRARY_PATH"] + ":/home/forth/asap4all/qub/gcc-5/gcc-5.4.0-install/lib64"
    os.environ["LD_LIBRARY_PATH"] = os.environ["LD_LIBRARY_PATH"] + ":."
    exists = os.path.exists("centroids/%s/%s_%s" % (region, year, week) )
    if exists:
	print ">>> Removing pre-existing version of output"
	os.system("rm centroids/%s/%s_%s" % (region, year, week) )
    res = os.system("./wind_kmeans -c 100 -r 5 -i %s -o centroids/%s/%s_%s" % (rsubfolder, region, year, week) )
    exists = os.path.exists("centroids/%s/%s_%s" % (region, year, week) )
    destExists = os.system("$HADOOP_PREFIX/bin/hdfs dfs -test -e /centroids/%s/%s_%s" % (region, year, week) )
    # Alternative way to move local results to hdfs:///centroids/forth ?  Unclear how works with directives in description file.
    if exists:
      print ">>> Output has been created"
      if destExists == 0:
          os.system("$HADOOP_PREFIX/bin/hdfs dfs -rm -r /centroids/%s/%s_%s" % (region, year, week))
      else:
          os.system("$HADOOP_PREFIX/bin/hdfs dfs -mkdir -p /centroids/%s" % region)
      os.system("$HADOOP_PREFIX/bin/hdfs dfs -copyFromLocal centroids/%s/%s_%s /centroids/%s/%s_%s" % (region, year, week, region, year, week))
    else:
      print "Output does not exist locally in centroids/", region, "/", year, "/", week

