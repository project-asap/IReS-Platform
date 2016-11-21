#!/bin/bash
source  $(dirname $0)/config.info       #loads the parameters
source  $(dirname $0)/common.sh         #loads the common functions

tmp_input=./tmp_text/
local_input=/root/Data/ElasticSearch_text_docs

for ((docs=100; docs<=100000; docs+=2000)); do

	printf "\n\nMoving $docs documents to $tmp_input...\n\n"
	./move_local.sh $local_input $tmp_input $docs >/dev/null;
	
	input_size=$(du -h -b $tmp_input | cut -f1)
	vector_size=100
	minDf=5

	asap run word2vec gensim $tmp_input $vector_size $minDf
	sleep 2
	asap report -e word2vec_gensim -cm -m input_size=$input_size \
					      vector_size=$vector_size \
					      minDf=$minDf \
					      docs=$docs
	
	rm -rf $tmp_input
done
