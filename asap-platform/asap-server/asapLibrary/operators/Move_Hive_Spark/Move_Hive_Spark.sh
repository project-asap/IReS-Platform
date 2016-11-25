#!/bin/bash

echo -e "Move_Hive_Spark\n"

export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
HDFS=/user/hive/warehouse
BASE=/mnt/Data/tmp
TABLE=$1
SCHEMA=$2
SPARK_PORT=$3

#echo "exporting table from HIVE"
#if [ ! -e /mnt/Data/tmp/$TABLE ]
#then
#	mkdir /mnt/Data/tmp/$TABLE
#	sudo chmod -R a+wrx /mnt/Data/tmp
#else
#	rm -r /mnt/Data/tmp/$TABLE/*
#fi
#$HADOOP_HOME/bin/hdfs dfs -copyToLocal $HDFS/$TABLE/* /mnt/Data/tmp/$TABLE
#wget http://master:50070/webhdfs/v1/user/hive/warehouse/$TABLE/$TABLE.csv?op=OPEN --output-document /mnt/Data/tmp/$TABLE/$TABLE.csv
#sudo chmod a+rw /mnt/Data/tmp/$TABLE/$TABLE.csv
#ls -lah /mnt/Data/tmp/$TABLE
#if [ ! -f /mnt/Data/tmp/$TABLE/$TABLE.csv ]
#then
#	for x in $(ls /mnt/Data/tmp/$TABLE/*);
#	do
#		echo "Copying file "$x
#		cat $x >> /mnt/Data/tmp/$TABLE/$TABLE.csv
#	done
#fi
#ls -lahtr /mnt/Data/tmp/$TABLE
#echo -e "Uploading $TABLE.csv to HDFS"
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
#$HADOOP_HOME/bin/hdfs dfs -moveFromLocal /mnt/Data/tmp/$TABLE/$TABLE.csv $HDFS
echo -e "Converting $TABLE.csv to parquet"
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.parquet
$SPARK_HOME/bin/spark-submit --executor-memory 2G --driver-memory 512M  --packages com.databricks:spark-csv_2.10:1.4.0 --master $SPARK_PORT convertCSV2Parquet.py $TABLE
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
#rm -r /mnt/Data/tmp/$TABLE
