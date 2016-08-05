#!/bin/bash
input=$1

export SPARK_HOME=/opt/spark
export PATH=$PATH:/$SPARK_HOME/bin
export MAHOUT_HOME=/opt/mahout-distribution-0.10.0
export PATH=$PATH:/$MAHOUT_HOME/bin
export HADOOP_HOME=/opt/hadoop-2.7.0
export PATH=$PATH:/$HADOOP_HOME/bin

MEM=$4
CORES=$3

xmdummy="-xm sequential"
chunk=1

seqfiles="sequence_files1"
hdfs dfs -rmr $seqfiles
#remove any previous files
#hdfs dfs -rm $output/* 2>/dev/null | wc -l | echo [PREP] Deleted $(cat) old sequence files

#mahout seqdirectory -i $input -o $seqfiles -c UTF-8 -chunk $chunk $xmdummy -ow

spark-submit --driver-memory 512M --executor-memory ${MEM}M --total-executor-cores $CORES spark_tfidf.py -i $1 -o $2
