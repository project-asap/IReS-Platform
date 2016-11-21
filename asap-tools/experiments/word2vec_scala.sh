#!/bin/bash
source  $(dirname $0)/config.info       #loads the parameters
source  $(dirname $0)/common.sh         #loads the common functions

hadoop_input=hdfs://master:9000/user/root/exp_text
local_input=/root/Data/ElasticSearch_text_docs

for ((docs=75000; docs<=100000; docs+=5000)); do
	hdfs dfs -rm -r $hadoop_input &>/dev/null
	printf "\n\nMoving data to hdfs...\n\n"
	asap move dir2sequence $local_input $hadoop_input $docs &> dir2sequence.out
	printf "\n\nDocs: $docs\n\n"

	input_size=$(hdfs_size $hadoop_input)
	vector_size=100
	minDf=5
	iterations=1

	echo Running Spark Word2Vec with k $k
	asap run word2vec spark_scala $hadoop_input
	asap report -e word2vec_spark_scala -cm -m input_size=$input_size \
						   vector_size=$vector_size \
						   minDf=$minDf \
						   iterations=$iterations \
						   points=$docs
	sleep 2
	
done;
