#!/bin/bash 

# libraries, etc
tools="$HOME/bin/lib/asapTools.jar"

export CHUNK=30


function kmeans #ENGINE K MAX_ITERATIONS 
{
engine=$1
shift
case "$engine" in
 	spark)
		$SPARK_HOME/bin/spark-submit ${ASAP_HOME}/spark/spark_kmeans_text.py -i $1 -k $2 -mi $3;;
	weka)
		${ASAP_HOME}/weka/kmeans_text_weka/kmeans_text_weka.sh $@ ;;
	mahout)
		${ASAP_HOME}/hadoop/mahout-kmeans/mahout_kmeans_text.sh $@ ;;
	cilk)
		iterations=$3
		echo max iters \($iterations\) ignored
		${ASAP_HOME}/centralized/hans_kmeans/kmeans/kmeans_arff -i $1 -c $2 &>$4
		#${ASAP_HOME}/hadoop/mahout-kmeans/mahout_kmeans_text.sh $@ ;;
esac

}

function tfidf #ENGINE INPUT OUTPUT MIN_DOCS  
{
	engine=$1
	shift
	case "$engine" in
	 	spark)
			echo +++++DEBUG+++++
			$SPARK_HOME/bin/spark-submit ${ASAP_HOME}/spark/spark_tfidf_v2.py -i $1 -o $2 -mdf $3;;
		weka)
			echo tfidf in weka
			${ASAP_HOME}/weka/kmeans_text_weka/tfidf_weka.sh $@ ;;
		mahout)
			echo tfidf in  mahout
			${ASAP_HOME}/hadoop/mahout-kmeans/mahout_tfidf.sh	$@ ;;
		*)
			echo No such tfidf;;
	esac

}

function word2vec #ENGINE INPUT OUTPUT
{
	engine=$1
	input=$2
	vector_size=$3
        min_df=$4
	shift
	case "$engine" in 
		pyspark)
			echo Word2Vec in pyspark 
			$SPARK_HOME/bin/spark-submit ${ASAP_HOME}/spark/spark_word2vec.py $input;;
		spark_scala)
			echo Word2Vec in Spark Scala
			$SPARK_HOME/bin/spark-submit --class Word2Vec ${ASAP_HOME}/spark/sparkops_2.10-1.0.jar $input;;
		gensim)
			echo Word2Vec in Gensim
			python ${ASAP_HOME}/centralized/Word2Vec/Word2Vec.py $input $vector_size $min_df;;

		*)
			echo Engine "$engine" does not exist;;
	esac
}


function lda
{
	engine=$1
	input=$2
	k=$3
	iterations=$4
	case "$engine" in
		scala)
			echo LDA in Spark_Scala
			$SPARK_HOME/bin/spark-submit --class LDA ${ASAP_HOME}/spark/sparkops_2.10-1.0.jar $input;;
		gensim)
			echo LDA in Gensim
			python ${ASAP_HOME}/centralized/LDA/LDA.py $input $k $iterations;;
		*)
			echo Engine "$engine" does not exist;;
	esac
}

function move # MOVE_OPERATION INPUT OUTPUT [COUNT]
{
	operation=$1
	shift
	case "$operation" in
		dir2arff)
			#${ASAP_HOME}/weka/kmeans_text_weka/convert_text_weka.sh $@ ;;
			count_arg=""
			if [ ! -z "$3" ]; then count_arg="-count $3"; fi
			java -jar $tools "dir2arff" -dir $1 $count_arg >$2;;
		dir2sequence)
			hadoop jar $tools dir2sequence $@ $CHUNK ;;
		mahout2spark)
			hadoop jar $tools mahout2spark $@;;
		reuters2sequence)
			hadoop jar $tools reuters2sequence $@ $CHUNK ;;
		dir2spark)
			pyspark ${ASAP_HOME}/spark/text_loader.py -i $1 -do $2 ;;
		
		*) 	# for all other operations assume asap-tools is used
			hadoop jar $tools $operation $@ ;;
	esac

}

