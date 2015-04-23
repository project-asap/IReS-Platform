#!/bin/bash
WEKA=/opt/npapa/weka.jar

input_dir=$1
virtual_dir=virt
rm -rf $virtual_dir 2>/dev/null
mkdir -p $virtual_dir/text

file=0

echo "linking docs"
for f in $input_dir/*
do
        #echo "Linking file - $(pwd)/$f"
        ((file+=1))
        ln -s $(pwd)/$f $(pwd)/$virtual_dir/text/$file
done

echo "converting to arff"
/opt/jdk1.7.0_71/bin/java -Xmx6g -cp ${WEKA} weka.core.converters.TextDirectoryLoader -dir $virtual_dir > data.arff

#tfidf
min_frequency=50

echo "TF/IDF"
/opt/jdk1.7.0_71/bin/java -Xmx6g -cp ${WEKA} weka.filters.unsupervised.attribute.StringToWordVector -R first-last -C -L -N 0 -W 99999999 -prune-rate -1.0 -stemmer weka.core.stemmers.NullStemmer -M ${min_frequency} -tokenizer "weka.core.tokenizers.WordTokenizer -delimiters \" \\r\\n\\t.,;:\\\'\\\"()?\!\"" -i data.arff -o tf_idf_data.arff

ls -ltr
