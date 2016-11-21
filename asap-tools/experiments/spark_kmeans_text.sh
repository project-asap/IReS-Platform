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
	echo -n "[EXPERIMENT] TF-IDF on $docs documents, minDF=$minDF: "
	
	input_size=$(hdfs_size $hadoop_input) 
	
	asap run tfidf spark $hadoop_input $spark_vectors $minDF &> spark_tfidf.out

	output_size=$(hdfs_size $spark_vectors)

	check_spark spark_tfidf.out
	dimensions=1048576

	echo $dimensions features, $(peek_time) sec
	
	asap report -e spark_tfidf -cm -m documents=$docs dimensions=$dimensions \
		minDF=$minDF input_size=$input_size output_size=$output_size

}

spark_kmeans(){
	input_size=$(hdfs_size $spark_vectors)

	echo -n "[EXPERIMENT] spark K-means with K=$k: "
	asap run kmeans spark $spark_vectors $k $max_iterations &> spark_kmeans.out
	
	output_size=0

	#check_spark spark_kmeans.out # IDK why this fails (OK is never printed)

	rm -r /tmp/spark* 2>/dev/null
	echo $(peek_time) sec
	
	#DEBUG show any exceptions but igore them
	cat spark_kmeans.out | grep Exception
	
	asap report -e spark_kmeans_text -cm -m documents=$docs k=$k dimensions=$dimensions \
		minDF=$minDF input_size=$input_size output_size=$output_size

}



spark2mahout(){
        docs=$1
        dimensions=$2

        # Move spark to mahout
        echo -n "[EXPERIMENT] Move Spark->Mahout on $docs documents "
        asap run move spark2mahout $spark_vectors $moved_mahout &> spark2mahout.out
	
	check spark2mahout.out
	input_size=$(hdfs_size $spark_vectors)	
	output_size=$(hdfs_size $moved_mahout)

        echo $(peek_time) sec
	
	asap report -e spark2mahout -cm -m documents=$docs minDF=$minDF dimensions=$dimensions \
		input_size=$input_size output_size=$output_size	

}

spark2arff(){
        docs=$1
        dimensions=$2

        # Move spark to arff
	input_size=$(hdfs_size $spark_vectors)
        echo -n "[EXPERIMENT] Move Spark->arff on $docs documents"
        
	asap run move spark2arff $spark_vectors $moved_arff &> arff2spark.out
	
	check arff2spark.out
	output_size=$(size $moved_arff)

        echo , $(peek_time) sec

	asap report -e spark2arff -cm -m documents=$docs minDF=$minDF dimensions=$dimensions \
		input_size=$input_size output_size=$output_size

}



for ((docs=min_documents; docs<=max_documents; docs+=documents_step)); do
	#re-load the parameters on each iteration for live re-configuration

	hdfs dfs -rm -r $hadoop_input &>/dev/null
	echo "[PREP] Loading $docs text files"
	asap run move dir2sequence $input_dir $hadoop_input $docs &> dir2sequence.out
	check dir2sequence.out

	
	#for (( minDF=max_minDF; minDF>=min_minDF; minDF-=minDF_step)); do
	minDF=10
		spark_tfidf $docs $minDF
	
		spark2mahout $docs $dimensions
		spark2arff $docs  $dimensions
		hdfs dfs -rm -r "/tmp/moved*" &>/dev/null

		for((k=min_k; k<=max_k; k+=k_step)); do
			spark_kmeans 
		done
	#done
done
