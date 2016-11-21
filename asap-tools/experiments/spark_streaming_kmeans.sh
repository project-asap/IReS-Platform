#!/bin/bash
data_file=~/Data/Result_W2V_IMR_New.csv

ss_kmeans=$ASAP_HOME/spark/streaming/streaming_kmeans_kafka.py
k_producer=$ASAP_HOME/kafka/kafka_file_producer.py
monitor=$ASAP_HOME/monitoring/monitor.py

kill_ss(){
	kill $ss_pid # send kill sig
	# wait for process to finish
	while kill -0 $ss_pid 2>/dev/null; do
	    sleep 0.5
	done
}

k_produce(){
	bytes=$($k_producer -f $data_file -l $lines | grep chars | awk '{print $2}')
}

interval=3
k_step=5
max_k=27
min_k=5
min_lines=1000
max_lines=1001000
lines_step=200000

#ks=(2 5 10 20 40)
ks=(10)
#lines_counts=(1000 10000 100000 300000 600000 1000000)
lines_counts=(1000 10000 50000 100000 300000)
intervals=(1 2 5 10)

for k in "${ks[@]}"; do
	for interval in "${intervals[@]}";do
		echo starting stream for k=$k and interv=$interval

		# start spark streaming job and keep its id
		spark-submit $ss_kmeans -i $interval -k $k &
		ss_pid=$!
		# wait for spark streaming to init
		sleep 20

		for lines in "${lines_counts[@]}"; do

			asap monitor start # start monitoring
			echo starting producing $lines lines
			k_produce # produce the kafka stream
			echo Produced $lines lines, $bytes bytes
			
			# wait for experiment to end and report streaming and monitoring metrics
			asap report -cm -cs -e streaming_kmeans \
				-m bytes=$bytes k=$k interval=$interval lines=$lines
		done
			kill_ss # kill the streaming job
			echo OK DONE
	done
done





