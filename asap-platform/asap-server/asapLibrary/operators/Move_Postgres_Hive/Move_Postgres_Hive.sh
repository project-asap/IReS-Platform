#!/bin/bash
echo "exporting table from postgres"
sudo -u postgres psql $1 -c "\copy (Select * From $2) To '/tmp/intermediate.csv' With CSV"

echo "loading table to hive"
export HADOOP_HOME='/opt/hadoop-2.6.0'
/opt/hive-1.1.0/bin/hive -e "DROP TABLE $2; CREATE TABLE $2 $3 ROW FORMAT DELIMITED FIELDS TERMINATED BY ','; LOAD DATA LOCAL INPATH '/tmp/intermediate.csv' OVERWRITE INTO TABLE $2"

rm /tmp/intermediate.csv