#!/bin/bash

echo -e "Move_Spark_Hive\n"_

export HADOOP_HOME=/opt/hadoop-2.7.0
export SPARK_HOME=/opt/spark
export HIVE_HOME=/root/vpapa/hive
HDFS=/user/hive/warehouse
TABLE=$1
SCHEMA=$2
SPARK_PORT=$3
SQL_QUERY="DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'; LOAD DATA LOCAL INPATH '/mnt/Data/tmp/$TABLE/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"

echo "Exporting table from Spark"
if [ ! -e /mnt/Data/tmp/$TABLE ]
then
	mkdir /mnt/Data/tmp/$TABLE
	sudo chmod -R a+wrx /mnt/Data/tmp
else
	rm -r /mnt/Data/tmp/$TABLE/*
fi
#convert parquet file to csv
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
$SPARK_HOME/bin/spark-submit --executor-memory 2G --driver-memory 512M  --packages com.databricks:spark-csv_2.10:1.4.0 --master $SPARK_PORT convertParquet2CSV.py $HADOOP_HOME $TABLE
$HADOOP_HOME/bin/hdfs dfs -copyToLocal $HDFS/$TABLE.csv/* /mnt/Data/tmp/$TABLE
ls -lah /mnt/Data/tmp/$TABLE
if [ -f /mnt/Data/tmp/$TABLE/$TABLE.csv ]
then
rm /mnt/Data/tmp/$TABLE/$TABLE.csv
fi
for x in $(ls /mnt/Data/tmp/$TABLE/part-*);
do
echo "Copying file "$x
      cat $x >> /mnt/Data/tmp/$TABLE/$TABLE.csv
done
ls -ltr /mnt/Data/tmp/$TABLE
#from parquet file double quotes have been added to string values
#and for this their length is changed. To restore their length the
#extra double quotes are removed with sed
sed -i 's/|\"/|/g' /mnt/Data/tmp/$TABLE/$TABLE.csv
sed -i 's/\"|/|/g' /mnt/Data/tmp/$TABLE/$TABLE.csv
#rm /mnt/Data/tmp/temp_sed
#head /mnt/Data/tmp/$TABLE/$TABLE.csv
chown -R postgres:postgres /mnt/Data/tmp/$TABLE/$TABLE.csv
ls -ltr /mnt/Data/tmp/$TABLE

echo "Loading table to Hive"
cd $HIVE_HOME
#$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY ','; LOAD DATA LOCAL INPATH '/mnt/Data/tmp/$TABLE/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"
$HIVE_HOME/bin/hive -e "$SQL_QUERY"

cd -

#clean
rm -r /mnt/Data/tmp
$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS/$TABLE.csv
