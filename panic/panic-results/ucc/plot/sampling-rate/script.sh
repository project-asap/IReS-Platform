#!/bin/bash

# script used to take as input a set of metric files containing the average error, etc of the trained models
# and depict them in the same plots vs the sampling rate

[ "$1" == "" ] && echo "I need METRICS_DIR as first argument" && exit 1 
METRICS_DIR=$1

FILE_PREFIX="metrics"
RATES=""
for i in $(ls $METRICS_DIR/metrics* | tr '-' '\t' | awk '{print $2}'); do
	RATES=$RATES"${i%.csv} "
done


LINES_PER_SAMPLER=11
MODELS=$(head -n 12 $METRICS_DIR/$FILE_PREFIX-0.05.csv | tail -n 9 | awk '{print $1}')




cleanup(){
	rm /tmp/data-*.csv;
}

initialize(){
for m in $MODELS; do
echo -e "SR\tMSE\tMean\tDeviation\tR" >/tmp/data-${m}.csv
done
}

plot_model(){
INPUT_FILE=$1
TITLE=$(basename $INPUT_FILE| tr '.' '\t' | tr '-' '\t' | awk '{print $2}')
gnuplot -p << EOF

set grid;
set xlabel "Sampling Rate"
set title '$TITLE'
set terminal png



set output '$TITLE-MSE.png'
set ylabel "MSE"
plot '$INPUT_FILE' using 1:2 with lines title 'Adaptive Sampling', \
	'$INPUT_FILE' using 1:6 with lines title 'Uniform Sampling', \
	'$INPUT_FILE' using 1:10 with lines title 'Random Sampling'


set output '$TITLE-Average.png'
set ylabel "Average Error"
plot '$INPUT_FILE' using 1:3 with lines title 'Adaptive Sampling', \
	'$INPUT_FILE' using 1:7 with lines title 'Uniform Sampling', \
	'$INPUT_FILE' using 1:11 with lines title 'Random Sampling'

set output '$TITLE-Deviation.png'
set ylabel "Deviation"
plot '$INPUT_FILE' using 1:4 with lines title 'Adaptive Sampling', \
	'$INPUT_FILE' using 1:8 with lines title 'Uniform Sampling', \
	'$INPUT_FILE' using 1:12 with lines title 'Random Sampling'


set output '$TITLE-R.png'
set ylabel "R"
plot '$INPUT_FILE' using 1:5 with lines title 'Adaptive Sampling', \
	'$INPUT_FILE' using 1:9 with lines title 'Uniform Sampling', \
	'$INPUT_FILE' using 1:13 with lines title 'Random Sampling'



EOF

}




initialize


for SR in $RATES; do
GREEDY_LINE=$(grep -n "Greed" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')
UNIFORM_LINE=$(grep -n "Uniform" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')
RANDOM_LINE=$(grep -n "RandomSampler" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')


for m in $MODELS; do 
	LINE=$(head  -n $[GREEDY_LINE+LINES_PER_SAMPLER] $METRICS_DIR/$FILE_PREFIX-$SR.csv | tail -n $[LINES_PER_SAMPLER-2] | grep $m)
	echo -ne "$SR\t$(echo $LINE| awk '{print $2"\t"$3"\t"$4"\t"$5}')" >> /tmp/data-${m}.csv
	LINE=$(head  -n $[UNIFORM_LINE+LINES_PER_SAMPLER] $METRICS_DIR/$FILE_PREFIX-$SR.csv | tail -n $[LINES_PER_SAMPLER-2] | grep $m)
	echo -ne "\t$(echo $LINE| awk '{print $2"\t"$3"\t"$4"\t"$5}')" >> /tmp/data-${m}.csv
	LINE=$(head  -n $[RANDOM_LINE+LINES_PER_SAMPLER] $METRICS_DIR/$FILE_PREFIX-$SR.csv | tail -n $[LINES_PER_SAMPLER-2] | grep $m)
	echo -e "\t$(echo $LINE| awk '{print $2"\t"$3"\t"$4"\t"$5}')" >> /tmp/data-${m}.csv
done

done


for m in $MODELS; do
plot_model /tmp/data-${m}.csv
done


OUTPUT_FOLDER=$2

if [ "$OUTPUT_FOLDER" != "" ]; then
	mv *.png $OUTPUT_FOLDER;
fi
