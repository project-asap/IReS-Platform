#!/bin/bash
source  $(dirname $0)/config.info 	#loads the parameters
source  $(dirname $0)/common.sh 	#loads the common functions


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
	echo -n "[EXPERIMENT] TF-IDF on $docs documents, minDF=$minDF: "
	input_size=$(hdfs_size $hadoop_input)
	asap run tfidf mahout $hadoop_input $tfidf_dir $minDF &> mahout_tfidf.out
	check mahout_tfidf.out

	output_size=$(hdfs_size $tfidf_dir)

	# find the dimensions of the output
	dimensions=$(hadoop jar ${TOOLS_JAR}  seqInfo  $tfidf_dir/dictionary.file-0 | grep Lenght: | awk '{ print $2 }')
	echo $dimensions features, $(peek_time) sec

	asap report -e mahout_tfidf -cm -m documents=$docs dimensions=$dimensions minDF=$minDF\
		input_size=$input_size output_size=$output_size

}

kmeans(){
	k=$1
	max_iterations=$2
	dimensions=$3
	echo -n "[EXPERIMENT] mahout K-means with K=$k: "
			
	#start monitoring
	in_size=$(hdfs_size $tfidfs_dir)
	asap run kmeans mahout $tfidf_dir $k $max_iterations $mahout_raw_clusters &> mahout_kmeans.out

	check mahout_kmeans.out
	out_size=$(hdfs_size $mahout_raw_clusters)
	echo $(peek_time) sec
	
	asap report -e mahout_kmeans_text -cm -m documents=$docs k=$k dimensions=$dimensions minDF=$minDF \
		input_size=$input_size output_size=$output_size

	#some housekeeping		    
	hdfs -rm -r $mahout_raw_clusters &> mahout_kmeans.out
}

mahout2arff (){
	docs=$1
	dimensions=$2


	# move mahout to arff
	echo -n "[EXPERIMENT] Move Mahout->arff on $docs documents "
	input_size=$(hdfs_size $tfidf_dir)
	asap run move mahout2arff $tfidf_dir $moved_arff &> mahout2arff.out

	check mahout2arff.out
	output_size=$(size $moved_arff)

	echo $dimensions features, $(peek_time) sec
	
	asap report -e mahout2arff -cm -m documents=$docs minDF=$minDF dimensions=$dimensions \
		input_size=$input_size output_size=$output_size

}

mahout2spark (){
	docs=$1
	dimensions=$2

	# Move mahout to spark
	echo -n "[EXPERIMENT] Move Mahout->Spark on $docs documents "

	asap run move mahout2spark $tfidf_dir $moved_spark &> mahout2spark.out
	
	check mahout2spark.out
	
	input_size=$(hdfs_size $tfidf_dir)
	output_size=$(hdfs_size $moved_spark)

	echo $dimensions features, $(peek_time) sec
	
	asap report -e mahout2spark -cm -m documents=$docs minDF=$minDF dimensions=$dimensions \
		input_size=$input_size output_size=$output_size

}


###################### Main Profiling Loops #########################

for ((docs=min_documents; docs<=max_documents; docs+=documents_step)); do

	echo "[PREP] Loading $docs text files"
	asap run move dir2sequence $input_dir $hadoop_input $docs &> dir2sequence.out
	check dir2sequence.out
	
	for (( minDF=max_minDF; minDF>=min_minDF; minDF-=minDF_step)); do

	
		#TF-IDF
		tfidf $docs $minDF

		#Mahout to Arff
		mahout2arff $docs $dimensions
		
		#Mahout to Spark
		mahout2spark $docs $dimensions

		hdfs dfs -rm -r "/tmp/moved*" &>/dev/null
	
	   	#Loop for the various values of K parameter
		for((k=min_k; k<=max_k; k+=k_step)); do
			kmeans $k $max_iterations $dimensions
		done
	done
done

exit
	

rm -rf tmp 2>/dev/null
