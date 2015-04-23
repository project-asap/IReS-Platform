#!/bin/bash

HADOOP_INSTALLATION_PATH="/opt/hadoop"
HBASE_INSTALLATION_DIR="/opt/hbase"

# Format namenode and start the hadoop cluster

$HADOOP_INSTALLATION_PATH/bin/hadoop namenode -format

sleep 10

$HADOOP_INSTALLATION_PATH/bin/start-dfs.sh

sleep 10

$HADOOP_INSTALLATION_PATH/bin/start-mapred.sh

sleep 10


$HBASE_INSTALLATION_DIR/bin/start-hbase.sh

echo "hadoop started"