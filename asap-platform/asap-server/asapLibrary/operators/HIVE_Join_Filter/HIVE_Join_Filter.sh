#!/bin/bash
export HADOOP_HOME='/home/bill/PhD/projects/yarn'
HIVE_HOME='/home/bill/PhD/projects/hive'

$HIVE_HOME/bin/hive -e "DROP TABLE $3; set hive.auto.convert.join=true; CREATE TABLE $3 ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' AS SELECT NATIONKEY, TOTALPRICE FROM $1 LEFT JOIN $2 ON $1.CUSTKEY=$2.CUSTKEY WHERE NATIONKEY<25;" 

