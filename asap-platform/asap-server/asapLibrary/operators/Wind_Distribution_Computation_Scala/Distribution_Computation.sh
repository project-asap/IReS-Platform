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
#pass command line arguments explicitly
#./submit.sh ta.DistributionComputation spark://localhost:7077 /output/trainingData /output
#pass command line arguments implicitly through description file
./submit.sh ta.DistributionComputation $1 $2 $3
echo -e "... submit.sh script for Distribution_Computation_Scala operator ended"
