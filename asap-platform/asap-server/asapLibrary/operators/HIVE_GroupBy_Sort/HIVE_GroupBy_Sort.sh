#!/bin/bash
export HADOOP_HOME='/opt/hadoop-2.6.0'
/opt/hadoop-2.6.0/bin/hadoop fs -rmr $2
/opt/hadoop-2.6.0/bin/hadoop fs -mkdir $2

/opt/hive-1.1.0/bin/hive -e "DROP TABLE IF EXISTS tmp_table; CREATE TABLE tmp_table ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' AS SELECT NATIONKEY, SUM(TOTALPRICE) AS SUM FROM $1 GROUP BY NATIONKEY ORDER BY SUM";

/opt/hadoop-2.6.0/bin/hadoop fs -mv /opt/warehouse/tmp_table/* $2/
/opt/hive-1.1.0/bin/hive -e "DROP TABLE tmp_table;"