#!/bin/bash

echo -e "Move_Hive_Spark\n"

export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
HDFS=/user/hive/warehouse
BASE=/mnt/Data/tmp
TABLE=$1
SCHEMA=$2
SPARK_PORT=$3

echo -e "Converting $TABLE.csv to parquet"
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.parquet
$SPARK_HOME/bin/spark-submit --executor-memory 2G --driver-memory 512M  --packages com.databricks:spark-csv_2.10:1.4.0 --master $SPARK_PORT convertCSV2Parquet.py $TABLE
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
#rm -r /mnt/Data/tmp/$TABLE
