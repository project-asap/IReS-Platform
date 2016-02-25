#!/bin/bash

<<INFO
Author				:	Papaioannou Vassilis
Last update			:	09/ 01/ 2016
Previous updates	:	none
Platform			:	ASAP IReS
Github				:	https://github.com/project-asap/IReS-Platform
Work package		:	Telecom analytics
Github				:	https://github.com/project-asap/telecom-analytics/blob/current/docs/Sociometer.md
INFO

<<DESCRIPTION
./submit.sh ta.DataFilter <master> <cdrPath> <output> <trainingSince (yyyy-MM-dd)> <trainingUntil (yyyy-MM-dd)> <testSince (yyyy-MM-dd)> <testUntil (yyyy-MM-dd or None)> <voronoiPath>

Input parameter:

master			:    the spark master URI
cdrPath			:    the input CDR dataset (HDFS or local)
output			:    the ouput path (an non existing HDFS or local directory)
trainingSince	:    the start date for the training period (format yyyy-MM-dd)
trainingUntil	:    the end date for the training period (format yyyy-MM-dd)
testSince		:    the start date for the test period (format yyyy-MM-dd)
testUntil		:    the end date for the training period (format yyyy-MM-dd) or None
voronoiPath		:    the path to the voronoi table: the set of towers ids in analysis.

Output:

Upon successful execution the <output>/trainingData & <output>/testData datasets
will be created.
DESCRIPTION


echo -e "Starting submit.sh script for Data_Filter_Spark operator ..."
#pass command line arguments explicitly
#./submit.sh ta.DataFilter spark://localhost:7077 /dataset_simulated /output 2015-06-01 2015-06-02 2015-06-03 None /voronoi
#pass command line arguments implicitly through description file
./submit.sh ta.DataFilter $1 $2 $3 $4 $5 $6 $7 $8
echo -e "... submit.sh script for Data_Filter_Spark operator ended"
