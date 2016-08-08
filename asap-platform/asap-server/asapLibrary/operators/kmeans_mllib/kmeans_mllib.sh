#!/bin/bash
export SPARK_HOME=/opt/spark
export PATH=$PATH:/$SPARK_HOME/bin
export MAHOUT_HOME=/opt/mahout-distribution-0.10.0
export PATH=$PATH:/$MAHOUT_HOME/bin
export HADOOP_HOME=/opt/hadoop-2.7.0
export PATH=$PATH:/$HADOOP_HOME/bin

echo "STARTING"
spark-submit --driver-memory 4G --executor-memory 5G spark_kmeans_text.py -i $1 -o $2 -k $3 -mi $4 -s $5
echo "DONE"

cp /tmp/kmeans.txt .
