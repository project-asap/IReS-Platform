#!/bin/bash

[ "$1" == "" ] && echo "I need METRICS_DIR as first argument" && exit 1 
INPUT_FILE=$1



DIMENSION_1=$(cat $INPUT_FILE | awk '{print $1}' | sort -n |uniq | grep --invert-match "[a-z]")
DIMENSION_2=$(cat $INPUT_FILE | awk '{print $2}' | sort -n |uniq | grep --invert-match "[a-z]")
DIMENSION_3=$(cat $INPUT_FILE | awk '{print $3}' | sort -n |uniq | grep --invert-match "[a-z]")



initialize(){
for j in $DIMENSION_2; do
echo "Nodes\tSize\tTime"> /tmp/data-3d-${j}.csv
done
}

plot_function(){

DATA_FILE=$1
CORES=$(basename $DATA_FILE | tr '-' '\t' | tr '.' '\t'| awk '{print $3}')
XLABEL="Cluster size"
XTICS=",2,3,4,5,6,7,8,9,10"
case $(basename $INPUT_FILE) in
	'pagerank.csv')
		TITLE="PageRank" && YLABEL="Size (x10^3 nodes)"
		YTICS="50,10,100"
		ZTICS="80,40,320"
		;;
	'terasort.csv')
		TITLE="Terasort" && YLABEL="Size (x10^6 tuples)"
		YTICS="10,10,50"
		ZTICS="0,100,700"
		;;
	'sssp.csv')
		TITLE="SSSP" && YLABEL="Size (x10^3 nodes)"
		YTICS="100,100,500"
		ZTICS="50,50,450"
		;;
esac
gnuplot -p << EOF

set grid

set title '$TITLE performance ($CORES cores/node)'
set xlabel '$XLABEL' rotate by -30
set ylabel '$YLABEL' rotate by -30
set zlabel 'Time (sec)' rotate by 90
set style line 1 lt 1 lw 5

set ytics $YTICS
set ztics $ZTICS

set terminal postscript eps size 7,5.0 enhanced color font 'Arial,31' 
set output 'output-${CORES}.eps'
splot '$DATA_FILE' using 1:2:3 with lines ls 1 lt rgb "#aa0000" title '$TITLE'

EOF
}

cleanup(){
for j in $DIMENSION_2; do
rm /tmp/data-3d-${j}.csv
done
}



initialize


for i in $DIMENSION_1;do
for j in $DIMENSION_2; do
for k in $DIMENSION_3;do
	echo -e "${i}\t${k}\t$(cat $INPUT_FILE | grep "${i}\s${j}\s${k}" | awk '{print $4}')" >> /tmp/data-3d-${j}.csv
done
done
for j in $DIMENSION_2; do
	echo >> /tmp/data-3d-${j}.csv
done
done


for j in $DIMENSION_2; do
plot_function /tmp/data-3d-${j}.csv
done





OUTPUT_FOLDER=$2

if [ "$OUTPUT_FOLDER" != "" ]; then
	mkdir $OUTPUT_FOLDER
	for i in *.eps; do
		epstopdf $i;
	done
	mv *.pdf $OUTPUT_FOLDER;
	rm *.eps
fi
