#!/usr/bin/env bash

csv_dataset=$1 # the input csv_dataset (an hdfs url)

w2v_model=$2
w2v_output=$3
#w2v_model=$work_dir/w2v_model_spark # the dir where the w2v model will be saved (hdfs)
#w2v_output=$work_dir/w2v_vectors/ # the output vectors of w2v (hdfs)

w2v_jar_path=imr_w2v_2.11-1.0.jar # path for the w2v jar file
### (1.2) Vectorize with W2V ###
spark-submit $w2v_jar_path sv $w2v_model $csv_dataset $w2v_output

