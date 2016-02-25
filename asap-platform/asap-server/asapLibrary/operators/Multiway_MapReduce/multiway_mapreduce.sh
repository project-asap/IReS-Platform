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
. resources/experiments/definitions.sh

./experiments.sh -e m,chainjoin -x 2
