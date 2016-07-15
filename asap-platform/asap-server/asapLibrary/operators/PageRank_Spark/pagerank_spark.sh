#!/bin/bash
inputFile=$1
outputFile=$2
iterations=${4%.*}


echo "Pagerank"

/opt/spark/bin/spark-submit \
	--class examples.JavaPageRank \
	--master spark://master:7077 \
	--executor-memory 1G \
	--total-executor-cores 9 \
	--jars file:///opt/guava-16.0.1.jar \
	file:///opt/testPageRank.jar \
	${inputFile} ${iterations} ${outputFile}
