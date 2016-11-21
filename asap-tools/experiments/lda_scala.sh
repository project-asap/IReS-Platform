#!/bin/bash
source  $(dirname $0)/config.info       #loads the parameters
source  $(dirname $0)/common.sh         #loads the common functions

hadoop_input=hdfs://master:9000/user/root/exp_text
local_input=/root/Data/ElasticSearch_text_docs

for ((docs=1000; docs<=20000; docs+=500)); do
	hdfs dfs -rm -r $hadoop_input &>/dev/null
	printf "\n\nMoving $docs documents to hdfs...\n\n"
	asap move dir2sequence $local_input $hadoop_input $docs &> dir2sequence.out
	#printf "\n\nDocs: $docs\n\n"
	printf "\n\nTFIDF\n\n"
	asap tfidf spark /user/root/exp_text /user/root/exp_text/tfidf 5
	
	input_size=$(hdfs_size $hadoop_input)
	iterations=1
	k=10

	#echo Running Spark Word2Vec with k $k
	asap run lda scala "$hadoop_input/tfidf/part*"
	asap report -e lda_spark_scala -cm -m input_size=$input_size iterations=$iterations k=$k docs=$docs
	sleep 2
done
