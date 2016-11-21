#!/usr/bin/env bash
source  $(dirname $0)/common.sh         #loads the common functions

full_training_data=~/Data/imr_training_suffled.csv
data_dir=~/Data/imr_workflow_central		# dir of intermediate data
result_dir=$data_dir/result			# the results of each step


training_data=$data_dir/input_data_raw.csv 	# raw training data (text)
training_norm=$data_dir/input_data_norm.csv	# normalized data (text)
training_vecs=$data_dir/data_vectors.csv	# vectorized data (tuple(vector))

w2v_model=~/Data/word2vectormodel_3108.bin	# W2V model location
lr_model=$data_dir/log_reg_model	# LogReg model location

# the ammounts of lines to test
tested_lines=(1000 2000 4000 10000 20000 50000 100000 200000 300000)

# global parameters
param="{'loss':'log','n_iter':100,'alpha':0.0001}" # no idea about what
model="SGDClassifier"				   # any of those do

#location of the scripts
wanalytics="python $ASAP_HOME/../imr-code/web-analytics/"

fsize(){
	du -sB1 $1 | awk '{print $1}'
}

preprocess(){
	
	echo -n "[PREPROCESS] Preproc on $lines lines: "	
	asap monitor start -mh master #start monitoring (only for host 'master')

	$wanalytics/normalize.py \
		--inputFile $training_data \
		--outputFile $training_norm\
		&> imr_preproc.out

   	$wanalytics/sampling.py \
		--inputFile $training_norm \
		--outputFile $data_dir/training_sample.csv \
		--minCount 2 --maxCount 30 \
		&>> imr_preproc.out
	
	asap monitor stop #stop monitoring
	
	in_bytes=$(fsize $training_data)	
	out_bytes=$(fsize $training_norm )
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_preprocess -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

w2v_train(){
	
	echo -n "[EXPERIMENT] W2V train on $lines lines: "    
	asap monitor start -mh master #start monitoring (only for host 'master')

	# run w2v train
	word2vec \
		-train $training_norm \
		-output $w2v_model \
		-cbow 0 -size 200 -window 10 -negative 10 -hs 0 -sample 1e-5 -threads 2 -binary 1 -iter 5 -min-count 10 \
		&> imr_w2v_train.out
	
	asap monitor stop

	in_bytes=$(fsize $training_norm)
	out_bytes=$(fsize $w2v_model)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
	asap report -e imr_w2v_train -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

w2v_vectorize(){

	echo -n "[EXPERIMENT] W2V vectorize on $lines lines: "    
	asap monitor start -mh master #start monitoring (only for host 'master')
	# vectorize is in 2 steps for some reason
	# Step 1.2.1 vectorize w2v (train) =================
	$wanalytics/vectorize.py \
		--inputFile $training_norm \
		--outputFile $training_vecs \
		--w2vSize 200 \
		--w2vModel $w2v_model \
		&> imr_w2v_vectorize.out
	
	asap monitor stop #stop monitoring
	in_bytes=$(fsize $training_norm)
	out_bytes=$(fsize $training_vecs)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
	
	asap report -e imr_w2v_vectorize -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

classifier_train(){
	
	# Step 2.1 train the classifier =================
	echo -n "[EXPERIMENT] Train classifier on $lines lines: "
	asap monitor start -mh master #start monitoring (only for host 'master')
	$wanalytics/train.py \
		--trainFile $training_vecs \
		--modelFile $lr_model \
		--batchSize 1000 \
		--classParam $param \
			&> imr_classifier_train.out
	asap monitor stop
	in_bytes=$out_bytes #input size is the output size of the previous step
	out_bytes=$(fsize $lr_model) # model size
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_train -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

classifier_predict(){

	# Step 2.2 predict =================
	echo -n "[EXPERIMENT] Predict with classifier on $lines lines: "
	asap monitor start -mh master #start monitoring (only for host 'master')
	$wanalytics/predict.py \
		--inputFile $training_vecs \
		--modelFile $lr_model \
		--batchSize 1000 \
			&> imr_classifier_predict.out
	asap monitor stop
	in_bytes=$out_bytes #input size is the output size of the previous step
	out_bytes=-1
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_predict -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}


############ main profiling loop ############

for lines in ${tested_lines[@] }
do
	# remove and recreate work dir
	rm -r $data_dir &>/dev/null; mkdir -p $data_dir
	
	# create new file with 'lines' lines
	head -n $lines $full_training_data > $training_data
	preprocess
	w2v_train
	w2v_vectorize
	classifier_train
	classifier_predict
done
