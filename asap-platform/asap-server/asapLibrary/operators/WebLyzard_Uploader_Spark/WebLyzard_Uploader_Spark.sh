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

source /home/forth/asap-venv/bin/activate

echo -e "Starting weblyzard uploader script ..."
SPARK_PORT=$1
OPERATOR=$2
PASSWORD=$3
SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master $SPARK_PORT $OPERATOR $JSON $PASSWORD > response_status

#validate through logs that anything went well
ls -lah
echo -e ""
echo -e ""
cat response_status
echo -e "... weblyzard uploader script ended"
