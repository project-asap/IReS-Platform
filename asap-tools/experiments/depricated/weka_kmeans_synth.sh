#!/bin/bash
source config.info 	#loads the parameters

sqlite3 results.db "CREATE TABLE IF NOT EXISTS weka_kmeans_synth (id INTEGER PRIMARY KEY AUTOINCREMENT, points INTEGER, k INTEGER, dimensions INTEGER, time INTEGER, date TIMESTAMP);"

rm weka_kmeans_synth.out &>/dev/null
for ((points=min_points; points<=max_points; points+=points_step)); do   
	for((dimensions=min_dimensions; dimensions<i=max_dimensions; dimensions+=dimensions_step)); do
		for((k=min_k; k<=max_k; k+=k_step)); do
			for ((i=1; i<=$runs; i++)); do
				#generate the data (if not exists)
				#the output of the generator is the filename
				input=$(../numerical_generator/generator_simple.py -n $points -d $dimensions -o ~/Data/synth_clusters 2>/dev/null)

				echo weka_kmeans for K=$k, dimensions=$dimensions file: $input
				tstart
				java -Xmx15g -jar ~/bin/lib/kmeans_weka.jar $input $k $max_iterations >weka_kmeans_synth.out
				time=$(ttime)
				sqlite3 results.db "INSERT INTO weka_kmeans_synth(points, k, dimensions, time, date)
				        VALUES( $points, $k, $dimensions, $time, CURRENT_TIMESTAMP);"
				#delete the data for the next run
				rm ~/Data/synth_clusters/*
			done
		done
	done
done
