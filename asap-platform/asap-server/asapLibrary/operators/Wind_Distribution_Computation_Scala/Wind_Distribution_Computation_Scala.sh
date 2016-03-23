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
/submit.sh ta.DistributionComputation <master> <trainingDataFile> <output>

Input parameters:

master				:    the spark master URI
trainingDataFile	:    the training dataset URI (HDFS or local) (created during
							the data filtering step)
output				:    the ouput path (an non existing HDFS or local directory)

Output: Upon successful execution the <output>/cpBase dataset will be created.

e.g.: 


DESCRIPTION

echo -e "Starting submit.sh script for Distribution_Computation_Scala operator ..."
REGION="roma"
TIMEFRAME="june-2015"
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master spark://131.114.136.218:7077 typical_distribution_computation.py $REGION $TIMEFRAME
echo -e "... submit.sh script for Distribution_Computation_Scala operator ended"
