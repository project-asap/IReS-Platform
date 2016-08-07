#!/bin/bash

export HADOOP_HOME=/opt/hadoop-2.7.0
export HIVE_HOME=/mnt/Data/tmp/hive

DATABASE=$1
TABLE=$2
SCHEMA=$3
HDFS=/user/hive/warehouse

echo -e "$DATABASE"
echo -e "$TABLE"
echo -e "$SCHEMA"

if [ ! -e /mnt/Data/tmp/$TABLE ]
then
	mkdir -p /mnt/Data/tmp/$TABLE
	sudo chmod -R a+wrx /mnt/Data/tmp
fi
echo "exporting table from postgres"
#sudo -u postgres psql -d $1 -c "COPY (SELECT * FROM $2) TO '//mnt/Data/tmp/intermediate.csv' WITH CSV (DELIMITER '|')"
sudo -u postgres psql -d $DATABASE -c "COPY (SELECT * FROM $TABLE) TO '/mnt/Data/tmp/$TABLE/$TABLE.csv' WITH  (DELIMITER '|', FORMAT 'csv')"
sudo scp /mnt/Data/tmp/$TABLE/$TABLE.csv master:/mnt/Data/
ssh master "ls -lah /mnt/Data/"
ssh master "$HADOOP_HOME/bin/hdfs dfs -moveFromLocal /mnt/Data/$TABLE.csv $HDFS/$TABLE.csv"

echo "loading table to hive"
cd $HIVE_HOME
#$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'; LOAD DATA LOCAL INPATH '/mnt/Data/tmp/$TABLE/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"
$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'; LOAD DATA INPATH '$HDFS/$TABLE.csv' OVERWRITE INTO TABLE $TABLE"
cd -

rm -r /mnt/Data/tmp/$TABLE
