#!/bin/bash
source  $(dirname $0)/config.info       #loads the parameters
source  $(dirname $0)/common.sh         #loads the common functions

tmp_input=./tmp_text
local_input=/root/Data/ElasticSearch_text_docs

for ((docs=6000; docs<=50000; docs+=2000)); do

	printf "\n\nMoving $docs documents to $tmp_input...\n\n"
	./move_local.sh $local_input $tmp_input $docs >/dev/null;
	
	input_size=$(du -h -b $tmp_input | cut -f1)
	k=10
	iterations=1

	asap run lda gensim $tmp_input $k $iterations
	sleep 2
	
	asap report -e lda_gensim - cm -m input_size=$input_size k=$k iterations=$iterations docs=$docs
	rm -rf $tmp_input
done
