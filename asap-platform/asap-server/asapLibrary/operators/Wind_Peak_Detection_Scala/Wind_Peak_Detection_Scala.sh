#!/bin/bash

<<INFO
Author				:	Papaioannou Vassilis
Last update			:	09/ 01/ 2016
Previous updates	:	none
Platform			:	ASAP IReS
Github				:	https://github.com/project-asap/IReS-Platform
Work package		:	Telecom analytics
Github				:	https://github.com/project-asap/telecom-analytics/blob/current/docs/PeakDetection.md
INFO

<<DESCRIPTION
./submit.sh ta.PeakDetection <master> <cpBaseFile> <testDataFile> <output> <binSize>

Input parameters:

master			:    the spark master URI
cpBaseFile		:    the cpBase dataset URI (HDFS or local)
testDataFile	:    the testData table URI (HDFS or local)
output			:    the ouput path
binSize			:    the bin size: the size of the events ratio bins (i.e. 10%)

Output: Upon successful execution the <output>/events & <output>/eventsFilter datasets will be created.

DESCRIPTION

source /home/forth/asap-venv/bin/activate

echo -e "Starting submit.sh script for Peak_Detection_Scala operator ..."
REGION="roma"
TIMEFRAME="june-2015"
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master spark://131.114.136.218:7077 peak_detection.py aree_roma.csv $REGION $TIMEFRAME
echo -e "... submit.sh script for Peak_Detection_Scala operator ended"
