#!/usr/bin/env bash

csv_dataset=$1

w2v_model=$2

rm -rf $w2v_model

w2v_jar_path=imr_w2v_2.11-1.0.jar # path for the w2v jar file


## (1.1) TRAIN W2V ###
spark-submit $w2v_jar_path sm $csv_dataset $w2v_model
