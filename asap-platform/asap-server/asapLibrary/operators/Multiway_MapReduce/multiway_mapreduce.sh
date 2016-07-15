#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: none
Host System			: Ubuntu >= 10.04 LTS
Hadoop				: 2.x
INFO

<<DESCRIPTION
The function that executes the 2nd experiment that examines Multiway and TwoByTwo
performance against reducers and available cpus
DESCRIPTION

#import definitions
#. resources/experiments/definitions.sh

#./experiments.sh -p m,chainjoin -e m,chainjoin -x 2
#./experiments.sh -p m,simplejoin -e m,simplejoin -x 1
echo -e "Going to execute the experiments.sh script"
./experiments.sh -p m,simplejoin
echo -e "Finished executing the experiments.sh script"
