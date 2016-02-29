#!/bin/bash
command="/opt/spark-1.3.1-bin-hadoop2.6/bin/spark-submit --master spark://192.168.5.134:7077 /opt/spark_kmeans_text.py -i $1 -o $2 -k $3 -mi $4"
echo $command

ssh slave1 "$command"
scp slave1:/tmp/kmeans.txt .