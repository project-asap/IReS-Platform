#!/bin/bash

DATA_FILE=$1

plot(){

INPUT_FILE=$1
PLOT_TITLE=$2
INDEX=$3
echo $INDEX
YTITLE="Mean Absolute Error"
gnuplot -p << EOF

set grid;
set xlabel "Sampling Rate"
set title '$PLOT_TITLE'
set style line 1 lt 1 lw 5 pt 4 ps 5 lc rgb "#aa0000" 
set style line 2 lt 1 lw 5 pt 2 ps 5 lc rgb "#00aa00" 
set style line 3 lt 1 lw 5 pt 3 ps 5 lc rgb "#0000aa" 
set key top right


set terminal postscript eps size 7,4.0 enhanced color font 'Arial,34' 
set output 'output-${PLOT_TITLE}.eps'

set ylabel "$YTITLE"
plot '$INPUT_FILE' index $INDEX using 1:2 with linespoints ls 1  ti "Adaptive Sampling", \
	'$INPUT_FILE' index $INDEX using 1:3 with linespoints ls 2 ti "Uniform Sampling"
#	'$INPUT_FILE' index $INDEX using 1:4 with linespoints ls 3 ti "Random Sampling"
EOF

}


plot $DATA_FILE "Terasort" 0
plot $DATA_FILE "PageRank" 1
plot $DATA_FILE "SSSP" 2

