#!/bin/bash
inputFile=$1
outputFile=$2
iterations=${4%.*}


echo "Pagerank"

/opt/hama-0.7.1/bin/hama jar /opt/hama-0.7.1/hama-examples-0.7.1.jar pagerank -i ${inputFile} -o ${outputFile} -t ${iterations}

