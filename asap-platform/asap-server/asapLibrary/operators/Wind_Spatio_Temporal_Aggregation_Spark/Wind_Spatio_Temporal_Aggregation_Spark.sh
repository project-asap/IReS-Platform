#!/bin/bash

<<INFO
Author				: Papaioannou Vassilis
Last update			: 28/ 03/ 2016 
Previous updates	: none
Host System			: Ubuntu
Hadoop				: 2.7.1
INFO

<<DESCRIPTION

DESCRIPTION

echo -e "Starting spatio_temporal_aggregation.py script ..."
SPARK_PORT=$1
OPERATOR=$2
DATASET=$3
IN_FILE=$4
REGION=$5
TIMEFRAME=$6
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master $SPARK_PORT $OPERATOR $DATASET $IN_FILE $REGION $TIMEFRAME

#validate through logs that anything went well
ls -lah
echo -e ""
echo -e ""
cat wl_timeseriesroma-june-july-2015-aree_roma.csvarea_presence
echo -e "... spatio_temporal_aggregation.py script ended"
