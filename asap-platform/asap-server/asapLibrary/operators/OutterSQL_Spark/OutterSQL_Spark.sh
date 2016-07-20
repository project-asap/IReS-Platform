#!/bin/bash

SPARK_PORT=$1
OPERATOR=$2
$HOST=$3
$YARN_HOME=$4
$SQL_QUERY=$5

SPARK_HOME=/home/bill/PhD/projects/spark-1.6.1
$SPARK_HOME/bin/spark-submit --master $SPARK_PORT $OPERATOR $HOST $YARN_HOME $SQL_QUERY
