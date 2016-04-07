#!/bin/bash

<<INFO
Author				: Papaioannou Vassilis
Last update			: 29/ 01/ 2016 
Previous updates	: none
Host System			: Ubuntu
Hadoop				: 2.7.1
INFO

<<DESCRIPTION
pyspark user_annotation.py <region> <timeframe>

Input parameters:

region		: a string containing the name of the region related to the dataset
timeframe	: a string containing the period related to the dataset

Output: It stores a file “sociometer<region>-<timeframe>” containing the percentage
		of user of each profile. E.g. roma-center, Resident, 0.34

DESCRIPTION

source /home/forth/asap-venv/bin/activate

echo -e "Starting user_annotation.py script ..."

SPARK_HOME=/home/forth/asap4all/spark-1.5.2-bin-hadoop2.6
$SPARK_HOME/bin/spark-submit --master spark://131.114.136.218:7077 stereo_type_classification.py roma 01-06-2015
echo -e "... user_annotation.py script ended"
