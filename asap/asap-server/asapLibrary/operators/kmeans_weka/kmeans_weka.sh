#!/bin/bash
WEKA=/opt/npapa/weka.jar

inputFile=$1
clusters=$2
iterations=$3
operator_out=$4


echo "K-Means"

/opt/jdk1.7.0_71/bin/java -Xmx6g -cp ${WEKA} weka.clusterers.SimpleKMeans \
             -N ${clusters} \
             -I ${iterations}  \
             -A "weka.core.EuclideanDistance -R first-last" \
             -t ${inputFile} \
              > $operator_out
