#!/bin/bash
CMD_HOME=/home/forth/asap/spark_sql_operator
$CMD_HOME/cmd.sh postgres "$1 $2" "SELECT * FROM $1 LEFT JOIN $2 ON $3 ;" hdfs
