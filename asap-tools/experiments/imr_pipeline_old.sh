#!/usr/bin/env bash
source  $(dirname $0)/common.sh         #loads the common functions

data_dir=~/Data
w2v_model=$data_dir/word2vectormodel_3108.bin
full_training_data=$data_dir/imr_training_suffled.csv
training_data=$data_dir/training__small.csv
intermediate_dir=$data_dir/web_analytics_intermediate

# global parameters
param="{'C':100}"
model="LogisticRegression"
nbJobs=4

#location of the scripts
wanalytics="python $ASAP_HOME/python/web-analytics"

fsize(){
	du -sB1 $1 | awk '{print $1}'
}

preprocess(){

	echo -n "[PREPROCESS] Preproc on $lines lines: "	
	asap monitor start -mh master #start monitoring (only for host 'master')

	#try remove/ re-creating the intermediate dir
	rm -r /home/cmantas/Data/web_analytics_intermediate &>/dev/null
	mkdir -p $intermediate_dir
	# Preprocess 1: normalize                                          }
	$wanalytics/normalizing.py \
		--inputFile  $training_data \
		--outputFile $intermediate_dir/normalized_vectors.csv \
		&> imr_preproc.out
	
	#Preprocess 2: category mapping 
	$wanalytics/CategoryMap.py \
		--inputFile $training_data \
		--outputFile $intermediate_dir/category_mappings.csv \
		&>> imr_preproc.out
	
	asap monitor stop #stop monitoring
	
	in_bytes=$(fsize $training_data)	
	out_bytes=$(fsize $intermediate_dir/normalized_vectors.csv)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_preprocess -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

w2v_train(){
	echo -n "[EXPERIMENT] W2V train on $lines lines: "    
	asap monitor start -mh master #start monitoring (only for host 'master')

	# preprpocess for w2v
	awk -F';' '{print $5" "$6" "$7}' $intermediate_dir/normalized_vectors.csv \
			> $intermediate_dir/normalized_vectors_only_text.csv
	# run w2v
	word2vec \
		-train $intermediate_dir/normalized_vectors_only_text.csv \
		-output $w2v_model \
		-cbow 0 -size 200 -window 10 -negative 10 -hs 0 -sample 1e-5 -threads 2 -binary 1 -iter 5 -min-count 10 \
		&> imr_w2v_train.out
	
	asap monitor stop
	in_bytes=$(fsize $intermediate_dir/normalized_vectors_only_text.csv)
	out_bytes=$(fsize $w2v_model)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
	asap report -e imr_w2v_train -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

w2v_vectorize(){
	echo -n "[EXPERIMENT] W2V vectorize on $lines lines: "    
	asap monitor start -mh master #start monitoring (only for host 'master')
	# vectorize is in 2 steps for some reason
	# Step 1.2.1 vectorize w2v (train) =================
	$wanalytics/vectorize_train.py \
		--inputFile $intermediate_dir/normalized_vectors.csv \
		--vectorFolder1 $intermediate_dir/word2vec/vec1 --vectorFolder3 $intermediate_dir/word2vec/vec3 \
		--vecMethod word2vec \
		--w2vModel $w2v_model \
		--nbJobs $nbJobs \
		&> imr_w2_vectorize.out
	
	# Step 1.2.2 vectorize w2v (valid) =================
	$wanalytics/vectorize_valid.py \
		--inputFile $training_data \
		--vectorFolder1 $intermediate_dir/word2vec/vec1 \
		--vectorFolder3 $intermediate_dir/word2vec/vec3 \
		--validVecFolder1 $intermediate_dir/word2vec/valid_vec_1 \
		--validVecFolder3 $intermediate_dir/word2vec/valid_vec_3 \
		--w2vModel $w2v_model \
		&> imr_w2v_vectorize.out
	
	asap monitor stop #stop monitoring
	in_bytes=$(fsize $intermediate_dir/normalized_vectors.csv)
	fs1=$(fsize $intermediate_dir/word2vec/vec1) #the 2 intermediate dir sizes
	fs2=$(fsize $intermediate_dir/word2vec/vec3)
	out_bytes=$(( fs1+fs2))
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)
	
	asap report -e imr_w2v_vectorize -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

classifier_train(){
	
	# Step 2.1 train the classifier =================
	echo -n "[EXPERIMENT] Train classifier on $lines lines: "
	asap monitor start -mh master #start monitoring (only for host 'master')
	$wanalytics/training.py \
		--vectorFolder1 $intermediate_dir/word2vec/vec1 \
		--vectorFolder3 $intermediate_dir/word2vec/vec3 \
		--modelFolder1 $intermediate_dir/word2vec/mod1 \
		--modelFolder3 $intermediate_dir/word2vec/mod3 \
		--classParameter "$param" --classifier $model \
		--nbJobs $nbJobs \
		&> imr_classifier_train.out
	asap monitor stop
	in_bytes=$out_bytes #input size is the output size of the previous step
	fs1=$(fsize $intermediate_dir/word2vec/mod1) #the 2 intermediate dir sizes
	fs2=$(fsize $intermediate_dir/word2vec/mod3)
	out_bytes=$(( fs1+fs2))
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_train -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}

classifier_predict(){

	# Step 2.2 predict =================
	echo -n "[EXPERIMENT] Predict with classifier on $lines lines: "
	asap monitor start -mh master #start monitoring (only for host 'master')
	$wanalytics/predicting.py \
		--preVecFolder1 $intermediate_dir/word2vec/valid_vec_1 \
		--preVecFolder3 $intermediate_dir/word2vec/valid_vec_3 \
		--modelFolder1 $intermediate_dir/word2vec/mod1 \
		--modelFolder3 $intermediate_dir/word2vec/mod3 \
		--predictFolder $intermediate_dir/predictions \
		--categoryMap $intermediate_dir/category_mappings.csv \
		&> imr_classifier_predict.out
	asap monitor stop
	in_bytes=$out_bytes #input size is the output size of the previous step
	out_bytes=$(fsize $intermediate_dir/predictions)
	echo $(peek_time)sec \(in: $in_bytes, out: $out_bytes, lines: $lines\)

	asap report -e imr_predict -cm -m input_bytes=$in_bytes output_bytes=$out_bytes lines=$lines

}


############ main profiling loop ############
max_lines=15786885
min_lines=10000000
lines_step=250000

for (( lines=min_lines; lines<=max_lines; lines+=lines_step))
do
	# create new file with 'lines' lines
	head -n $lines $full_training_data > $training_data
	preprocess
	w2v_train
	w2v_vectorize
	classifier_train
	classifier_predict
done
