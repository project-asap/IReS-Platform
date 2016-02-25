#!/bin/bash

<<INFO
Author				: Papaioannou Vassilis
Last update			: 29/ 01/ 2016 
Previous updates	: none
Host System			: Ubuntu
Hadoop				: 2.7.1
INFO

<<DESCRIPTION
pyspark clustering.py <region> <timeframe> <archetipi> <k> <percentage>

Input parameters:

region	 	: a string containing the name of the region related to the dataset
timeframe	: a string containing the period related to the dataset
archetipi	: a csv files containing typical calling profiles for each label.
				E.g.: Resident->typical resident profiles, etc..
k		 	: the number of centroids to be computed
percentage	: the percentage of profiles to use for centroids computation

Output:

It stores a files “centroids<region>-<timeframe>” containing the association
between each centroid and the user type. E.g. Centroid1->resident, etc
DESCRIPTION

echo -e "Starting clustering.py script ..."
#pass command line arguments explicitly
#pyspark clustering.py roma 06-2015 archetipi.csv 100 0.4
#pass command line arguments implicitly through description file
pyspark clustering.py $1 $2 $3 $4 $5
echo -e "... clustering.py script ended"
