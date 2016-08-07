#!/bin/bash

export HIVE_HOME=/root/vpapa/hive

SQL_QUERY=`cat $1`
#SQL_QUERY="set mapreduce.job.reducers=5; DROP TABLE IF EXISTS PART_AGG; $SQL_QUERY"
#SQL_QUERY="$SQL_QUERY"
QUERY_PATH=`pwd`

echo -e "$SQL_QUERY"

cd $HIVE_HOME
$HIVE_HOME/bin/hive -e "DROP TABLE IF EXISTS PART_AGG; $SQL_QUERY"
#$HIVE_HOME/bin/hive -e "show tables"
#$HIVE_HOME/bin/hive -e "select count(*) from part_agg"

cd -
