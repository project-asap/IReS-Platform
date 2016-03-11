#!/bin/bash
HOME=`eval echo ~$USER`


dataset=$1    
w2v_model=$2                                                                                                                               
output=$3
path=/root/imr-code/web-analytics/operators
                           
echo ======\> Normalize
python $path/normalize.py --inputFile ./$dataset --outputFile ./normalized.csv
                                                                                                         
echo ======\> W2V Vectorize
python $path/vectorize.py --inputFile ./normalized.csv --outputFile ./$output --w2vSize 200 --w2vModel ./$w2v_model
