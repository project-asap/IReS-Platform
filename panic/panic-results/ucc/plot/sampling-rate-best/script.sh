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

plot_model(){
INPUT_FILE=$1
TITLE=$(basename $INPUT_FILE| tr '.' '\t' | tr '-' '\t' | awk '{print $2}')
echo $TITLE
case $TITLE in
	r)
		YTITLE="R^2"
		YRANGE="[0.3:1]"
		;;
	mse)
		YTITLE="MSE"
		YRANGE="[0:]"
		;;
	average)
		YTITLE="Average Error"
		YRANGE="[0:]"
		;;
	deviation)
		YTITLE="Deviation"
		YRANGE="[0:]"
		;;
	
esac
case $(basename $METRICS_DIR) in
	sssp)
		PLOT_TITLE='SSSP'
		;;
	pagerank)
		PLOT_TITLE='PageRank'
		;;
	terasort)
		PLOT_TITLE='Terasort'
		;;
esac
gnuplot -p << EOF

set grid;
set xlabel "Sampling Rate"
set title '$PLOT_TITLE'
set style line 1 lt 1 lw 5 pt 4 ps 5 lc rgb "#aa0000" 
set style line 2 lt 1 lw 5 pt 2 ps 5 lc rgb "#00aa00" 
set style line 3 lt 1 lw 5 pt 3 ps 5 lc rgb "#0000aa" 
set key bottom right
set xtics 0.05,0.025,0.2

set yrange $YRANGE

set terminal postscript eps size 7,4.0 enhanced color font 'Arial,34' 
set output 'output-${TITLE}.eps'

set ylabel "$YTITLE"
plot '$INPUT_FILE' using 1:2 with linespoints ls 1  title 'Adaptive Sampling', \
	'$INPUT_FILE' using 1:3 with linespoints ls 2 title 'Uniform Sampling', \
	'$INPUT_FILE' using 1:4 with linespoints ls 3 title 'Random Sampling'
EOF

}



echo -e "SR\tAdaptive\tUniform\tRandom" > /tmp/sr-mse.csv
echo -e "SR\tAdaptive\tUniform\tRandom" > /tmp/sr-average.csv
echo -e "SR\tAdaptive\tUniform\tRandom" > /tmp/sr-deviation.csv
echo -e "SR\tAdaptive\tUniform\tRandom" > /tmp/sr-r.csv
for SR in $RATES; do
GREEDY_LINE=$(grep -n "Greed" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')
UNIFORM_LINE=$(grep -n "Uniform" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')
RANDOM_LINE=$(grep -n "RandomSampler" $METRICS_DIR/$FILE_PREFIX-$SR.csv  | tr ':' '\t' | awk '{print $1}')


MIN_GRE=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[GREEDY_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $2}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 ) 

MIN_UNI=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[UNIFORM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $2}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 )

MIN_RAN=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[RANDOM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | grep -v "[a-z]"| awk '{print $2}' | sort -n | head -n 1)

echo -e "$SR\t$MIN_GRE\t$MIN_UNI\t$MIN_RAN" >> /tmp/sr-mse.csv

MIN_GRE=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[GREEDY_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $3}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 ) 

MIN_UNI=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[UNIFORM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $3}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 )

MIN_RAN=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[RANDOM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | grep -v "[a-z]"| awk '{print $3}' | sort -n | head -n 1)

echo -e "$SR\t$MIN_GRE\t$MIN_UNI" >> /tmp/sr-average.csv


MIN_GRE=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[GREEDY_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $4}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 ) 

MIN_UNI=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[UNIFORM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $4}' |grep -v "[a-zA-Z]"| sort -n | head -n 1 )

MIN_RAN=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[RANDOM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | grep -v "[a-z]"| awk '{print $4}' | sort -n | head -n 1)

echo -e "$SR\t$MIN_GRE\t$MIN_UNI" >> /tmp/sr-deviation.csv

MIN_GRE=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[GREEDY_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $5}' |grep -v "[a-zA-Z]"| sort -n  | tail -n 2 | head -n 1 ) 

MIN_UNI=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[UNIFORM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $5}' |grep -v "[a-zA-Z]"| sort -n | tail -n 2 | head -n 1 )


MIN_RAN=$(cat $METRICS_DIR/$FILE_PREFIX-$SR.csv | head -n $[RANDOM_LINE+LINES_PER_SAMPLER] | tail -n $LINES_PER_SAMPLER | awk '{print $5}' |grep -v "[a-zA-Z]"| sort -n | tail -n 2 | head -n 1 )

echo -e "$SR\t$MIN_GRE\t$MIN_UNI\t$MIN_RAN" >> /tmp/sr-r.csv
done


plot_model /tmp/sr-mse.csv
plot_model /tmp/sr-average.csv
plot_model /tmp/sr-deviation.csv
plot_model /tmp/sr-r.csv

for i in *.eps; do
	epstopdf $i;
	rm $i;
done

OUTPUT_DIR=$2

if [ "$OUTPUT_DIR" != "" ]; then
	mkdir $OUTPUT_DIR
	mv *.pdf $OUTPUT_DIR;
fi
