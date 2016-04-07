#!/bin/bash

<<INFO
Author				:	Papaioannou Vassilis
Last update			:	28/ 03/ 2016
Previous updates	:	09/ 01/ 2016
Platform			:	ASAP IReS
Github				:	https://github.com/project-asap/IReS-Platform
Work package		:	Telecom analytics
Github				:	https://github.com/project-asap/telecom-analytics/blob/current/docs/PeakDetection.md
INFO

<<DESCRIPTION

DESCRIPTION

source /home/forth/asap-venv/bin/activate

echo -e "Starting data_filter.py script for Data_Filter_Spark operator ..."
SPARK_PORT=$1
OPERATOR=$2
DATASET=$3
IN_FILE=$4
REGION=$5
TIMEFRAME=$6
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master $SPARK_PORT $OPERATOR $DATASET $REGION $TIMEFRAME
echo -e "... data_filter.py script for Data_Filter_Spark operator ended"
