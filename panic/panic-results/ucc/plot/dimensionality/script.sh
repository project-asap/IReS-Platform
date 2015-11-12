#!/bin/bash

gnuplot -p << EOF

set auto x
set style data histogram
set style fill pattern border -1
set boxwidth 0.9
set xtics ("PageRank - 0.10" 0, "PageRank - 0.20" 1, "Terasort - 0.10" 2, "Terasort - 0.20" 3, "SSSP - 0.10" 4, "SSSP - 0.20" 5)
set yrange [0.0:1.0]

set ylabel "R^2"


set terminal postscript eps size 7,5.0 enhanced color font 'Arial,34' 
set output 'dimensionality.eps'

set xtics rotate by -45
set key out horiz
set key bottom
set grid 
set title "Accuracy vs Dimensions"
plot 'data2.dat' using 2  lc rgb "#004400" fs pattern 8 ti col, \
	'' using 3 lc rgb "#200000" fs pattern 11 ti col 
EOF

epstopdf dimensionality.eps

rm dimensionality.eps
