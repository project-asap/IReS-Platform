#!/bin/bash


HADOOP_INSTALLATION_PATH="/opt/hadoop"
HAMA_INSTALLATION_PATH="/opt/hama/"

HADOOP_BIN="$HADOOP_INSTALLATION_PATH/bin/hadoop"
HAMA_BIN="$HAMA_INSTALLATION_PATH/bin/hama"

HADOOP_JAR="$HADOOP_INSTALLATION_PATH/hadoop-examples-1.2.1.jar"
HAMA_JAR="$HAMA_INSTALLATION_PATH/hama-examples-0.6.4.jar"

DATASET_URL="http://snf-562459.vm.okeanos.grnet.gr/"

TIMEFILE="/tmp/times.csv"
LOGS="/tmp/jobs.log"
echo -n > $TIMEFILE
echo -n > $LOGS


TIME_CMD="/usr/bin/time -f %e\t%U\t%S -o $TIMEFILE --append"

SLAVES=$(cat /etc/hosts | grep slave | wc -l)
TASKS_PER_SLAVE=$(ssh slave1 "cat /proc/cpuinfo | grep processor | wc -l")
REDUCERS=$[SLAVES*TASKS_PER_SLAVE]

$HADOOP_BIN fs -mkdir /data/
# # Terasort benchmark , a.k.a. TeraBad
# for MILLIONS in 10 20 30 40 50; do 
# 	curl $DATASET_URL/tera-${MILLIONS}M.txt.gz | gzip -d | hadoop fs -put - /data/tera-${MILLIONS}M.txt
# 	echo -ne "Terasort-${MILLIONS}\t" >>$TIMEFILE
# 	$TIME_CMD $HADOOP_BIN jar $HADOOP_JAR terasort /data/ /output/tera-${MILLIONS}M 1>>$LOGS 2>>$LOGS
# 	tail -n 1 $TIMEFILE
# 	$HADOOP_BIN fs -rmr /data/tera-${MILLIONS}M.txt /output/tera-${MILLIONS}M 1>>$LOGS 2>>$LOGS
# done



# # Pagerank benchmark
# EDGES=50
# for THOUSANDS in 50 60 70 80 90 100; do
#  	curl $DATASET_URL/page-${THOUSANDS}k.txt.gz | gzip -d | hadoop fs -put - /data/page-${THOUSANDS}k.txt
# 	echo -ne "PageRank-${THOUSANDS}-${EDGES}\t" >> $TIMEFILE
# 	$TIME_CMD $HAMA_BIN jar $HAMA_JAR pagerank /data/page-${THOUSANDS}k.txt /output/pagerank-${THOUSANDS}-${EDGES} 1000 1>>$LOGS 2>>$LOGS
# 	tail -n 1 $TIMEFILE
# 	$HADOOP_BIN fs -rmr /data/page-${THOUSANDS}k.txt /output/pagerank-${THOUSANDS}-${EDGES} 1>>$LOGS 2>>$LOGS
# done


# # Terasort benchmark - scalable
# for MILLIONS in 10 20 30 40 50; do 
# curl $DATASET_URL/tera-${MILLIONS}M.txt.gz | gzip -d | $HADOOP_BIN fs  -put - /data/tera-${MILLIONS}M.txt
# echo -ne "Terasort-${MILLIONS}\t" >>$TIMEFILE
# $TIME_CMD $HADOOP_BIN jar $HADOOP_JAR terasort -Dmapred.reduce.tasks=$REDUCERS /data/ /output/tera-${MILLIONS}M 1>>$LOGS 2>>$LOGS
# tail -n 1 $TIMEFILE
# $HADOOP_BIN fs -rmr /data/tera-${MILLIONS}M.txt /output/tera-${MILLIONS}M 1>>$LOGS 2>>$LOGS
# done

# SSSP
NODE_ID=1
for THOUSANDS in 50 100 200 300 400 500; do
curl $DATASET_URL/sssp-${THOUSANDS}k.txt.gz | gzip -d | $HADOOP_BIN fs  -put - /data/sssp-${THOUSANDS}k.txt
echo -ne "SSSP-${THOUSANDS}\t" >> $TIMEFILE
$TIME_CMD $HAMA_BIN jar $HAMA_JAR sssp $NODE_ID /data/sssp-${THOUSANDS}k.txt /output/sssp-${THOUSANDS}k $REDUCERS 1>>$LOGS 2>>$LOGS
tail -n 1 $TIMEFILE
$HADOOP_BIN fs -rmr /data/sssp-${THOUSANDS}k.txt /output/sssp-${THOUSANDS}k 1>>$LOGS 2>>$LOGS
done

echo "Benchmark results are ready and can be found in $TIMEFILE"

cat $TIMEFILE

exit 0
