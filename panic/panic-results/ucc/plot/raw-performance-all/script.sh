#!/bin/bash

# script that creates all the performance metrics 
# given a formatted result file (from the deployments

[ "$1" == "" ] && echo "Need datafile as first argument " && exit 1
DATAFILE=$1
 
DIM_1=$(cat $DATAFILE | awk '{print $1}' | sort -g |uniq | grep --invert-match "[a-z]")
DIM_2=$(cat $DATAFILE | awk '{print $2}' | sort -g |uniq | grep --invert-match "[a-z]")
DIM_3=$(cat $DATAFILE | awk '{print $3}' | sort -g |uniq | grep --invert-match "[a-z]")


plot_function(){

SIZE=$1

gnuplot -p << EOF


set grid
set title 'Raw performance for size $SIZE'
set xlabel "Nodes"
set ylabel "Time (sec)"

set xrange [2:10]
set yrange [50:]


set terminal png
set output 'raw-$SIZE.png'
plot '/tmp/plot-temp-1-$SIZE.csv' using 1:2 with lines title '1 core', \
	'/tmp/plot-temp-2-$SIZE.csv' using 1:2 with lines title '2 cores', \
	'/tmp/plot-temp-4-$SIZE.csv' using 1:2 with lines title '4 cores'






EOF
}

cleanup(){
	rm /tmp/plot-temp*
}


for i in $DIM_1; do
	for j in $DIM_2; do
		for k in $DIM_3; do
			VALUE=$(cat $DATAFILE | grep "$i\s$j\s$k"| awk '{print $4}');
			echo -e "${i}\t$VALUE" >> /tmp/plot-temp-${j}-${k}.csv
		done
	done
done

for i in $DIM_3; do
	plot_function $i;
done


cleanup
