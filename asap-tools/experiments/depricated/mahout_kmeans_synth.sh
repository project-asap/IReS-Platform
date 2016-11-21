#!/bin/bash
source config.info 	#loads the parameters
source experiment.sh	#loads the experiment function
PARSER_JAR=~/bin/lib/asapTools.jar

output_file="mahout_kmeans_synth.out"

sqlite3 results.db "CREATE TABLE IF NOT EXISTS mahout_kmeans_synth (id INTEGER PRIMARY KEY AUTOINCREMENT, points INTEGER, k INTEGER, dimensions INTEGER, time INTEGER, date TIMESTAMP);"

for ((points=min_points; points<=max_points; points+=points_step)); do   
	for((dimensions=min_dimensions; dimensions<=max_dimensions; dimensions+=dimensions_step)); do
			for((k=min_k; k<=max_k; k+=k_step)); do
				
				#generate data (the output of the generator is the filename)
				echo "[PREP] Generate"
				input=$(../numerical_generator/generator_simple.py -n $points -d $dimensions -o ~/Data/synth_clusters 2>/dev/null)

				echo "[PREP] CSVs to Sequence File"
				hadoop jar ${PARSER_JAR} loadCSV ${input} synthetic_seq localInput >/dev/null
				
				tstart
				echo "[EXPERIMENT] mahout K-Means for $points points, $dimensions dimensions, K=$k"
				../hadoop/mahout-kmeans/mahout_kmeans_synth.sh synthetic_seq $clusters $max_iterations &> $output_file
				time=$(ttime)
				check $output_file
				sqlite3 results.db "INSERT INTO mahout_kmeans_synth(points, k, dimensions, time, date)
						        VALUES( $points, $k, $dimensions, $time, CURRENT_TIMESTAMP);"
                		
				#delete the data for the next run
				rm $input
			done
	done
done

rm -rf tmp 2>/dev/null
