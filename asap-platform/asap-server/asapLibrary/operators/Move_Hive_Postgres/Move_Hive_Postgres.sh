#!/bin/bash

export HADOOP_HOME='/home/bill/PhD/projects/yarn'
HIVE_HOME='/home/bill/PhD/projects/hive'

DATABASE=$1
TABLE=$2
SCHEMA=$3
SQL_QUERY="DROP TABLE $TABLE; CREATE TABLE $TABLE $SCHEMA; COPY $TABLE FROM '/tmp/out' WITH DELIMITER AS '|';"


echo "exporting table from HIVE"
mkdir t1
$HADOOP_HOME/bin/hadoop fs -copyToLocal $HIVE_HOME/warehouse/$2/* t1
rm /tmp/out
for x in $(ls t1/*);
do
        #echo $x
        cat $x >> /tmp/out
done
chown -R postgres:postgres /tmp/out
ls -ltr 
rm -r t1

echo "loading table to POSTGRES"
sudo -u postgres psql $DATABASE -c $SQL_QUERY
rm /tmp/out
