#!/bin/bash

echo -e "Move_Postgres_Spark\n"

<<<<<<< HEAD
<<<<<<< HEAD
export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
=======
#export HADOOP_HOME=/opt/hadoop-2.7.0
#export SPARK_HOME=/opt/spark
export HADOOP_HOME=/home/hadoop/yarn
export SPARK_HOME=/home/hadoop/spark
>>>>>>> temp
=======
export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
>>>>>>> 679b7257e992f967a6c90fdd205f40a21e7f2014

DATABASE=$1
TABLE=$2
SCHEMA=$3
SPARK_PORT=$4
HDFS=/user/hive/warehouse

echo -e "DATABASE = " $DATABASE
echo -e "TABLE = " $TABLE
echo -e "SCHEMA = " $SCHEMA
echo -e "SPARK_PORT = " $SPARK_PORT

if [ ! -e /mnt/Data/tmp/$TABLE ]
then
	mkdir -p /mnt/Data/tmp/$TABLE
	sudo chmod -R a+wrx /mnt/Data/tmp
fi
echo "exporting table from postgres"
sudo -u postgres psql -d $DATABASE -c "COPY (SELECT * FROM $TABLE) TO '/mnt/Data/tmp/$TABLE/$TABLE.csv' WITH (DELIMITER '|', FORMAT csv)"
head /mnt/Data/tmp/$TABLE/$TABLE.csv
ls -la /mnt/Data/tmp/$TABLE/*.csv
echo -e "Uploading $TABLE.csv to HDFS"
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
$HADOOP_HOME/bin/hdfs dfs -moveFromLocal /mnt/Data/tmp/$TABLE/$TABLE.csv $HDFS
echo -e "Converting $TABLE.csv to parquet"
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.parquet
$SPARK_HOME/bin/spark-submit --executor-memory 2G --driver-memory 512M  --packages com.databricks:spark-csv_2.10:1.4.0 --master $SPARK_PORT convertCSV2Parquet.py $TABLE
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
<<<<<<< HEAD
rm -r /mnt/Data/tmp
=======
rm -r /mnt/Data/tmp/$TABLE
>>>>>>> 679b7257e992f967a6c90fdd205f40a21e7f2014
