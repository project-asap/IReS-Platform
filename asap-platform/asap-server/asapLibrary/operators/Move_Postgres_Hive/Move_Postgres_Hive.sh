#!/bin/bash

export HADOOP_HOME='/home/bill/PhD/projects/yarn'
HIVE_HOME='/home/bill/PhD/projects/hive'

DATABASE=$1
TABLE=$2
SCHEMA=$3
SQL_QUERY="DROP TABLE $TABLE; CREATE TABLE $TABLE $SCHEMA ROW FORMAT DELIMITED FIELDS TERMINATED BY ','; LOAD DATA LOCAL INPATH '/tmp/intermediate.csv' OVERWRITE INTO TABLE $TABLE"


echo "exporting table from postgres"
sudo -u postgres psql $DATABASE -c "\copy (SELECT * FROM $TABLE) To '/tmp/intermediate.csv' With CSV"

echo "loading table to hive"
$HIVE_HOME/bin/hive -e $SQL_QUERY

rm /tmp/intermediate.csv
