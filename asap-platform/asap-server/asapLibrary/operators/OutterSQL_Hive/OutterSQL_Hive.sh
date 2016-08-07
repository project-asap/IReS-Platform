#!/bin/bash

export HIVE_HOME=/mnt/Data/tmp/hive

SQL_QUERY=`cat $1`
SQL_QUERY="DROP TABLE IF EXISTS FINAL_RESULTS; $SQL_QUERY"
QUERY_PATH=`pwd`

echo -e "$SQL_QUERY"

cd $HIVE_HOME
$HIVE_HOME/bin/hive -e "$SQL_QUERY"
#$HIVE_HOME/bin/hive -e "show tables"
#$HIVE_HOME/bin/hive -e "select count(*) from FINAL_RESULTS"

cd -
