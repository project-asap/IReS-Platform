#!/bin/bash

echo -e "Move_Hive_Postgres\n"

export HADOOP_HOME=/opt/hadoop-2.7.0
HDFS=/user/hive/warehouse
DATABASE=$1
TABLE=$2
SCHEMA=$3

echo "exporting table from HIVE"
if [ ! -e /mnt/Data/tmp/$TABLE ]
then
	mkdir -p /mnt/Data/tmp/$TABLE
	sudo chmod -R a+wrx /mnt/Data/tmp
else
	rm /mnt/Data/tmp/$TABLE/*
fi
$HADOOP_HOME/bin/hdfs dfs -copyToLocal $HDFS/$TABLE/* /mnt/Data/tmp/$TABLE
if [ ! -f /mnt/Data/tmp/$TABLE/$TABLE.csv ]
then
	for x in $(ls /mnt/Data/tmp/$TABLE/*);
	do
		#echo $x
		cat $x >> /mnt/Data/tmp/$TABLE/$TABLE.csv
	done
fi
chown -R postgres:postgres /mnt/Data/tmp/$TABLE/$TABLE.csv
ls -ltr 

echo "loading table to POSTGRES"
sudo -u postgres psql -d $DATABASE -c "DROP TABLE $TABLE;"
sudo -u postgres psql -d $DATABASE -c "CREATE TABLE $TABLE $SCHEMA;"
sudo -u postgres psql -d $DATABASE -c "COPY $TABLE FROM '/mnt/Data/tmp/$TABLE/$TABLE.csv' WITH DELIMITER AS '|';"
rm -r /mnt/Data/tmp/$TABLE
