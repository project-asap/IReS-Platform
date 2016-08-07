#!/bin/bash
WEKA=/opt/weka.jar

inputFile=$1
clusters=$2
iterations=$3
operator_out=$4


echo "K-Means"

java -Xmx6g -cp ${WEKA} weka.clusterers.SimpleKMeans \
             -N ${clusters} \
             -I ${iterations}  \
             -A "weka.core.EuclideanDistance -R first-last" \
             -t ${inputFile} \
              > $operator_out
