#!/bin/bash

<<INFO
Author				: Papaioannou Vassilis
Last update			: 29/ 01/ 2016 
Previous updates	: none
Host System			: Ubuntu
Hadoop				: 2.7.1
INFO

<<DESCRIPTION
pyspark user_profilo.py <folder> <spatial_division> <region> <timeframe>

Input parameters:

folder			: the hdfs folder where the dataset is located. In order to let
					the profiles be computed, it needs at least 3 weeks of data.
					Dataset is assumed to be splitted into days (e.g. one day =
					one csv file).
spatial_division: A csv file containing the spatial region of each GSM tower. E.g.
					“RM619D1;city_center”.
region			: a string containing the name of the region related to the dataset
timeframe		: a string containing the period related to the dataset

Output:

It stores the profiles (as Pickle file) into the folder /profiles<region>-<timeframe>.

Profiles are in the format:

	user_id->[(region,week n.,workday/weekend, timeframe,number of presence),….]

DESCRIPTION

echo -e "Starting user_profilo.py script ..."
#pass command line arguments explicitly
#pyspark user_profilo.py dataset centro_roma.csv roma 06-2015
#pass command line arguments implicitly through description file
pyspark user_profilo.py $1 $2 $3 $4
echo -e "... user_profilo.py script ended"
