#!/bin/bash
source  $(dirname $0)/config.info 	#loads the parameters
source  $(dirname $0)/common.sh 	#loads the common functions


output_file="spark_kmeans_text.out"

input_dir=~/Data/ElasticSearch_text_docs
TOOLS_JAR=~/bin/lib/asapTools.jar


#create HDFS files
hadoop_input=/user/$USER/input/kmeans_text_seqfiles
hdfs dfs -rm -r $hadoop_input &>/dev/null
hdfs dfs -mkdir -p $hadoop_input &>/dev/null

spark_vectors=/tmp/spark_tfidf
moved_mahout=/tmp/moved_mahout; hdfs dfs -mkdir -p $moved_mahout
moved_arff=/tmp/moved_vectors.arff

spark_tfidf(){
	docs=$1
	minDF=$2

	# TF/IDF
	hdfs dfs -rm -r $spark_vectors &>/dev/null
	echo -n "[EXPERIMENT] TF-IDF on $docs documents, on $slaves slaves: "
	
	input_size=$(hdfs_size $hadoop_input) 
	
	asap run tfidf spark $hadoop_input $spark_vectors $minDF &> spark_tfidf.out

	output_size=$(hdfs_size $spark_vectors)

	check_spark spark_tfidf.out
	dimensions=1048576

	echo $dimensions features, $(peek_time) sec
	
	asap report -e spark_tfidf_multi -cm -m documents=$docs dimensions=$dimensions \
		minDF=$minDF input_size=$input_size output_size=$output_size slaves=$slaves

}

spark_kmeans(){
	input_size=$(hdfs_size $spark_vectors)

	echo -n "[EXPERIMENT] spark K-means with K=$k on $slaves slaves: "
	asap run kmeans spark $spark_vectors $k $max_iterations &> spark_kmeans.out
	
	output_size=0

	#check_spark spark_kmeans.out # IDK why this fails (OK is never printed)

	rm -r /tmp/spark* 2>/dev/null
	echo $(peek_time) sec
	
	#DEBUG show any exceptions but igore them
	cat spark_kmeans.out | grep Exception
	
	asap report -e spark_kmeans_multi -cm -m documents=$docs k=$k dimensions=$dimensions \
		minDF=$minDF input_size=$input_size output_size=$output_size slaves=$slaves

}


for (( slaves=min_slaves; slaves<=max_slaves; slaves+=slaves_step))
 do
         reconfigure_spark $slaves
         for ((docs=min_documents; docs<=max_documents; docs+=documents_step)); do
 
 
                 echo "[PREP] Loading $docs text files"
                 asap run move dir2sequence $input_dir $hadoop_input $docs &> dir2sequence.out
                 check dir2sequence.out

                 #use fixed minDF
                 minDF=$min_minDF
                 #TF-IDF
		 spark_tfidf $docs $minDF

                 #Use fixed K
                 k=$max_k
		 spark_kmeans
         done
 
 done

