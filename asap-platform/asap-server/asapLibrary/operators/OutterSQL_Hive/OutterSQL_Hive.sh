#!/bin/bash

HIVE_HOME='/home/bill/PhD/projects/hive'

SQL_QUERY=$1

$HIVE_HOME/bin/hive -f $SQL_QUERY
