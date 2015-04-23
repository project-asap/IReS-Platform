#!/bin/bash
export JAVA_HOME='/opt/jdk1.7.0_71/'
input=$1
output=$2
max_iterations=$3
K=$4

mahout="/opt/mahout-distribution-0.9/bin/mahout"

${mahout} kmeans \
          -i ${input}/tfidf-vectors/ \
          -o $output \
          -dm org.apache.mahout.common.distance.CosineDistanceMeasure \
          -c /tmp/mahout_rand_clusters \
          -x ${max_iterations} \
          -k ${K}\
          -ow --clustering