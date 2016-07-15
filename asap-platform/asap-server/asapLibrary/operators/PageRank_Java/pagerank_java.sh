#!/bin/bash
inputFile=$1
outputFile=$2
iterations=${3%.*}


echo "Pagerank"

/opt/hadoop-2.7.0/bin/hadoop jar testPageRank.jar examples.PageRank ${inputFile} ${outputFile} ${iterations}

