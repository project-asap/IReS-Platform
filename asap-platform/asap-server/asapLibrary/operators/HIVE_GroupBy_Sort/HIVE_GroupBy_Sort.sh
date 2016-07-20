#!/bin/bash
export HADOOP_HOME='/home/bill/PhD/projects/yarn'
HIVE_HOME='/home/bill/PhD/projects/hive'

$HADOOP_HOME/bin/hadoop fs -rmr $2
$HADOOP_HOME/bin/hadoop fs -mkdir $2

$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS tmp_table; CREATE TABLE tmp_table ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' AS SELECT NATIONKEY, SUM(TOTALPRICE) AS SUM FROM $1 GROUP BY NATIONKEY ORDER BY SUM";
$HADOOP_HOME/bin/hadoop fs -mv /opt/warehouse/tmp_table/* $2/
$HIVE_HOME/bin/hive -e "DROP TABLE tmp_table;"
