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

echo -e "Starting submit.sh script for Peak_Detection_Scala operator ..."
#pass command line arguments explicitly
#./submit.sh ta.PeakDetection spark://localhost:7077 /output/cpBase /output/testData /output 0.1
#pass command line arguments implicitly through description file
./submit.sh ta.PeakDetection $1 $2 $3 $4 $5
echo -e "... submit.sh script for Peak_Detection_Scala operator ended"
