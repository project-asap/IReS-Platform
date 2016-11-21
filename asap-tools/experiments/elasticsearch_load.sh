#!/bin/bash
source  $(dirname $0)/config.info 	#loads the parameters
echo Removing old docs
rm -r  ~/Data/ElasticSearch_text_docs
python ../elasticsearch/rest_loader.py total $max_documents window $window  output $documents_data_dir $proxy
