#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 26/ 10/ 2015
Previous updates	: none
Host System			: Ubuntu 10.04 LTS+
Hadoop				: 2.x
INFO

<<DESCRIPTION
Remove Hadoop temporary folder, tmp and format Hadoop namenode.
DESCRIPTION

#import definitions
#. /home/hadoop/resources/experiments/definitions.sh

datanode=`jps | grep DataNode`
nodemanager=`jps | grep NodeManager`

if [[ -z "$datanode" ]]
then
        echo -e "ERROR: DataNode is not running at $HOSTNAME"
        exit
fi

if [[ -z "$nodemanager" ]]
then
        echo -e "ERROR: NodeManager is not running at $HOSTNAME"
        exit
fi

echo -e "Healthy"
