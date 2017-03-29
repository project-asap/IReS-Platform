#!/bin/bash
JARS='joda-time-2.8.2.jar'
SMASTER=$1
SPARK_HOME=$2
DRIVER_MEMORY=$3
EXECUTOR_MEMORY=$4
SUBMIT=$SPARK_HOME'/bin/spark-submit'
TARGET=./telecomanalytics_2.10-1.1.jar
CLASS=PresenceNestedBulk
PROPERTIES=$SPARK_HOME'/conf/spark-defaults.conf'
if [ -z $SMASTER ]
then
    $SUBMIT --verbose --jars $JARS --class $CLASS --driver-memory $DRIVER_MEMORY --executor-memory $EXECUTOR_MEMORY --properties-file $PROPERTIES $TARGET $1 $5 $6 $7 "$8" $9 ${10}
else
    $SUBMIT --verbose --jars $JARS --class $CLASS --master $SMASTER --driver-memory $DRIVER_MEMORY --executor-memory $EXECUTOR_MEMORY --properties-file $PROPERTIES $TARGET $1 $5 $6 $7 "$8" $9 ${10}
fi
