#!/bin/bash
source  $(dirname $0)/config.info 	#loads the parameters
source  $(dirname $0)/common.sh 	#loads the common functions
source  $(dirname $0)/cluster_workers_manager.sh #loads the cluster manager


input_dir=~/Data/ElasticSearch_text_docs
TOOLS_JAR=~/bin/lib/asapTools.jar


#create HDFS files
hadoop_input=./input/kmeans_text_seqfiles
mahout_raw_clusters=/tmp/clusters_raw
hdfs dfs -rm -r $hadoop_input
hdfs dfs -mkdir -p $hadoop_input

tfidf_dir=/tmp/mahout_tfidf
moved_arff=/tmp/moved_vecs.arff
moved_spark=/tmp/moved_spark; hdfs -mkdir -p $moved_spark &>/dev/null


tfidf (){
	docs=$1
	minDF=$2


	# TF/IDF
	echo -n "[EXPERIMENT] TF-IDF on $docs documents, $slaves slaves,  minDF=$minDF: "
	input_size=$(hdfs_size $hadoop_input)
	asap run tfidf mahout $hadoop_input $tfidf_dir $minDF &> mahout_tfidf.out
	check mahout_tfidf.out

	output_size=$(hdfs_size $tfidf_dir)

	# find the dimensions of the output
	dimensions=$(hadoop jar ${TOOLS_JAR}  seqInfo  $tfidf_dir/dictionary.file-0 | grep Lenght: | awk '{ print $2 }')
	echo $dimensions features, $(peek_time) sec

	asap report -e mahout_tfidf_multi -cm -m documents=$docs dimensions=$dimensions minDF=$minDF\
		input_size=$input_size output_size=$output_size slaves=$slaves

}

kmeans(){
	k=$1
	max_iterations=$2
	dimensions=$3
	echo -n "[EXPERIMENT] mahout K-means with K=$k slaves=$slaves: "
			
	#start monitoring
	in_size=$(hdfs_size $tfidfs_dir)
	asap run kmeans mahout $tfidf_dir $k $max_iterations $mahout_raw_clusters &> mahout_kmeans.out

	check mahout_kmeans.out
	out_size=$(hdfs_size $mahout_raw_clusters)
	echo $(peek_time) sec
	
	asap report -e mahout_kmeans_multi -cm -m documents=$docs k=$k dimensions=$dimensions minDF=$minDF \
		input_size=$input_size output_size=$output_size slaves=$slaves

	#some housekeeping		    
	hdfs -rm -r $mahout_raw_clusters &> mahout_kmeans.out
}



###################### Main Profiling Loops #########################


max_documents=100000
min_documents=300


for (( slaves=min_slaves; slaves<=max_slaves; slaves+=slaves_step))
do
	reconfigure_yarn $slaves
	for ((docs=min_documents; docs<=max_documents; docs+=documents_step)); do
	
		echo "[PREP] Loading $docs text files"
		asap run move dir2sequence $input_dir $hadoop_input $docs &> dir2sequence.out
		check dir2sequence.out
	
		#use fixed minDF
		minDF=$min_minDF
		#TF-IDF
		tfidf $docs $minDF
	
	   	#Use fixed K
		k=$max_k
		kmeans $k $max_iterations $dimensions
	done

done


	

rm -rf tmp 2>/dev/null
