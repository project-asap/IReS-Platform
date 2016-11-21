#!/bin/bash
source  $(dirname $0)/config.info 	#loads the parameters
source  $(dirname $0)/common.sh         #loads the common functions

rm $operator_out &>/dev/null


input_dir=~/Data/ElasticSearch_text_docs
mkdir -p $virtual_dir/text &>/dev/null

tmp_dir=/tmp/kmeans_weka
arff_vectors=$tmp_dir/tfidf.arff
arff_data=$tmp_dir/data.arff

moved_mahout=/tmp/moved_mahout; hdfs dfs -mkdir -p $moved_mahout &>/dev/null
moved_spark=/tmp/moved_spark; hdfs dfs -mkdir -p $moved_spark &>/dev/null


tfidf(){
	docs=$1
	minDF=$2
	input_size=$(size $arff_data)

	asap run tfidf weka $arff_data $arff_vectors $minDF &>weka_tfidf.out
	
	output_size=$(size $arff_vectors)
	check weka_tfidf.out

	dimensions=$(cat $arff_vectors | grep @attribute | wc -l)
	(( dimensions=dimensions-1 ))

	echo $dimensions features, $(peek_time) secs

	asap report -e weka_tfidf -cm -m documents=$docs dimensions=$dimensions minDF=$minDF input_size=$input_size output_size=$output_size

}

kmeans(){
	k=$1
	max_iterations=$2
	dimensions=$4
	docs=$3
	
	echo -n "[EXPERIMENT] weka_kmeans_text for k=$k, $docs documents, $dimensions dimensions: "
	input_size=$(size $arff_vectors)
	
	asap run kmeans weka $arff_vectors $k $max_iterations weka_clusters.out &>weka_kmeans.out
	check weka_kmeans.out
	output_size=$(size weka_clusters.out)
	echo $(peek_time) secs
	
	asap report -e weka_kmeans_text -cm -m documents=$docs k=$k dimensions=$dimensions minDF=$minDF input_size=$input_size output_size=$output_size
	
}


kmeans_cilk(){
	k=$1
	max_iterations=$2
	dimensions=$4
	docs=$3
	
	echo -n "[EXPERIMENT] cilk_kmeans_text for k=$k, $docs documents, $dimensions dimensions: "
	input_size=$(size $arff_vectors)
	
	asap run kmeans cilk $arff_vectors $k $max_iterations cilk_kmeans.out
	check cilk_kmeans.out #untested, probably does not work
	output_size=0
	echo $(peek_time) secs
	
	asap report -e cilk_kmeans_text -cm -m documents=$docs k=$k dimensions=$dimensions minDF=$minDF input_size=$input_size output_size=$output_size
	
}


arff2mahout (){
        docs=$1
        dimensions=$2

        echo -n "[EXPERIMENT] Move arff->Mahout on $docs documents, $dimensions "
	
        asap run move arff2mahout $arff_vectors $moved_mahout &> arff2mahout.out

        #check arff2mahout.out
	input_size=$(size $arff_vectors)
	output_size=$(hdfs_size $moved_mahout)

        echo  $(peek_time) sec
	
	# save in DB
        asap report -e arff2mahout -cm -m documents=$docs minDF=$minDF dimensions=$dimensions input_size=$input_size output_size=$output_size

}


arff2spark (){
        docs=$1
        dimensions=$2
        
	# Move mahout to spark
        echo -n "[EXPERIMENT] Move arff->Spark on $docs documents"
	
        asap run move arff2spark $arff_vectors $moved_spark &> arff2spark.out

	check arff2spark.out
	
	input_size=$(size $arff_vectors)
	output_size=$(hdfs_size $moved_spark)

        echo $dimensions features, $(peek_time) sec
        
	# save in DB
	asap report -e arff2spark -cm -m documents=$docs minDF=$minDF dimensions=$dimensions input_size=$input_size output_size=$output_size

}


#################### Main Profiling Loop ####################

for ((docs=min_documents; docs<=max_documents; docs+=documents_step)); do

	echo "[PREP]: Converting text to arff"
	#convert to arff
	asap run move dir2arff $input_dir $arff_data $docs &> dir2arff.out
	check dir2arff.out

	
	for (( minDF=max_minDF; minDF>=min_minDF; minDF-=minDF_step)); do
		echo -n "[EXPERIMENT] weka tf-idf for $docs documents, minDF=$minDF:  "
		
		#tfidf
		tfidf $docs $minDF

		#arff2mahout
		arff2mahout $docs $dimensions
		
		#arff2spark
		arff2spark $docs $dimensions
																		
	    	for((k=min_k; k<=max_k; k+=k_step)); do
			kmeans $k $max_iterations $docs $dimensions
			kmeans_cilk $k $max_iterations $docs $dimensions
		done #K parameter loop
		
		# if we got less features than we asked we need not ask for more
		if ((dimensions<asked_features));then
			echo No need to add more dimensions, continuing;break
		fi

	done #asked dimensions loop
done #documents count loop

exit
	

rm -rf tmp 2>/dev/null
