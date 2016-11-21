#!/usr/bin/env bash
source  $(dirname $0)/common.sh         #loads the common functions

tested_lines=(1000 2000 4000 6000 10000 20000 50000 100000 150000 200000 300000 500000)

local_csv=~/Data/imr_training_suffled.csv # the local csv dataset that contains
		# the real (big) available dataset we will be getting subsets of
		# for the experiments
hdfs_csv=hdfs://master:9000/tmp/imr_raw.csv # the input csv file (an hdfs url)
category=1 # the category to use
categories=~/Data/imr_labels.json  # a json file holding all possible labels for each category


##############  intermediate  data #########################3###
work_dir=$(dirname $hdfs_csv)/intermediate # dir of intermediate/output data (hdfs)
w2v_model=$work_dir/w2v_model_spark # the dir where the w2v model will be saved (hdfs)
w2v_output=$work_dir/w2v_vectors/ # the output vectors of w2v (hdfs)
lr_model=$work_dir/lr_model # the dir where the logistic regression model will be saved (hdfs)
class_output=$work_dir/classification_output # the output of the classification

############## abbreviations ##############
w2v_jar_path=~/bin/lib/imr_w2v_2.11-1.0.jar # path for the w2v jar fileA
# spark submit that includes a helper .py file (for brevity)
submit_py="spark-submit --py-files $ASAP_HOME/spark/imr_workflow/imr_tools.py"


w2v_train(){
        echo -n "[EXPERIMENT] W2V Train (Spark) on $lines lines: "        
        asap monitor start

	spark-submit $w2v_jar_path sm \
		$hdfs_csv $w2v_model &>spark_imr_w2v.log

	asap monitor stop
	in_bytes=$(hdfs_size $hdfs_csv)
	out_bytes=$(hdfs_size $w2v_model)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
        asap report -e imr_w2v_train_spark -cm \
		-m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

w2v_vectorize(){
        
	echo -n "[EXPERIMENT] W2V Vectorize  on $lines lines: "        
        asap monitor start
	
	spark-submit $w2v_jar_path sv \
		$w2v_model $hdfs_csv $w2v_output &> spark_imr_vectorize.log
	
	asap monitor stop
	in_bytes=$(hdfs_size $hdfs_csv)
	out_bytes=$(hdfs_size $w2v_output)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
        asap report -e imr_w2v_vectorize_spark -cm \
		-m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}


train(){

        echo -n "[EXPERIMENT] Train LR model (spark)  on $lines lines: "        
        asap monitor start

	$submit_py $ASAP_HOME/spark/imr_workflow/imr_classification.py \
		train $w2v_output \
		--model  $lr_model \
		--labels $categories \
		--category $category \
		&> spark_imr_train.log
	
	asap monitor stop
	in_bytes=$(hdfs_size $w2v_output)
	out_bytes=$(hdfs_size $lr_model)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
        asap report -e imr_lr_train_spark -cm \
		-m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

classify(){

        echo -n "[EXPERIMENT] Classify on $lines lines: "        
        asap monitor start
	$submit_py $ASAP_HOME/spark/imr_workflow/imr_classification.py \
		classify $w2v_output \
	        --output $class_output \
	        --model  $lr_model \
	        --labels $categories \
		--category $category \
		&> spark_imr_classify.log
	
	asap monitor stop
	in_bytes=$(hdfs_size $w2v_output)
	out_bytes=$(hdfs_size $class_ouput)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
        asap report -e imr_classify_spark -cm \
		-m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}



for lines in ${tested_lines[@] }
do
	# create/clean up dirs, etc
	hdfs dfs -rm -r $work_dir &>/dev/null
	hdfs dfs -mkdir -p $work_dir 

	# create the input dataset in HDFS from the local file
	head -n $lines $local_csv | hdfs dfs -put -f - $hdfs_csv
	
	w2v_train
	w2v_vectorize
	train 		# train a LR Model
	classify	# using that model
	
done
