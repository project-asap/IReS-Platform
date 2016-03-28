#!/bin/bash

<<INFO
Author				:	Papaioannou Vassilis
Last update			:	28/ 03/ 2016
Previous updates	:	09/ 02/ 2016
Platform			:	ASAP IReS
Github				:	https://github.com/project-asap/IReS-Platform
Work package		:	Telecom analytics
Github				:	https://github.com/project-asap/telecom-analytics/blob/current/docs/PeakDetection.md
INFO

<<DESCRIPTION
DESCRIPTION

echo -e "Starting typical_distribution_computation.py script for Distribution_Computation_Scala operator ..."
SPARK_PORT=$1
OPERATOR=$2
REGION=$3
TIMEFRAME=$4
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master $SPARK_PORT $OPERATOR $REGION $TIMEFRAME
REGION="roma"
TIMEFRAME="june-2015"
echo -e "... typical_distribution_computation.py script for Distribution_Computation_Scala operator ended"
