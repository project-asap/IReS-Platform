#!/bin/bash

HADOOP_INSTALLATION_PATH="/opt/hadoop"
HAMA_INSTALLATION_PATH="/opt/hama/"

# Format namenode and start the hadoop cluster

$HADOOP_INSTALLATION_PATH/bin/hadoop namenode -format

$HADOOP_INSTALLATION_PATH/bin/start-dfs.sh
sleep 30

$HADOOP_INSTALLATION_PATH/bin/start-mapred.sh
sleep 20

$HAMA_INSTALLATION_PATH/bin/start-bspd.sh
sleep 20
