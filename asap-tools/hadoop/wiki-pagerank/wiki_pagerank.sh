home=$(dirname $0)
hdfs_input="input/wikipedia"
hdfs_output="output/wiki_pagerank"
initial="/tmp/wiki_pagerank/initial"
unordered="/tmp/wiki_pagerank/unordered"
files=$(hdfs dfs -ls input/wikipedia | awk '{print $8}')
for f in $files
do
	echo File: $f
	##### 	PR PreProcess
	hdfs dfs -rm $hdfs_output/* &>/dev/null
	bytes=$(hls $f | awk '{print $5}')
	mb=$(( bytes/1024/1024 ))
	echo PR prerocess for $mb MB
       	hdfs dfs -rm -r /tmp/wiki_pagerank &>/dev/null
	date1=$(date +"%s")
	hadoop jar $home/target/*.jar PreProcessTask $f $initial 2> PR_preproc.out
	date2=$(date +"%s")
        diff=$(($date2-$date1))
	echo "* Took $diff seconds"
	echo "*" $(cat PR_preproc.out | grep Launched| awk '{print $6 $7 }')
	hdfs dfs -rm $f

	###### 	PageRank Iteration ########
	bytes=$(hls $initial/part* | awk '{print $5}')
	mb=$(( bytes/1024/1024 ))
	echo PR Iteration for $mb MB
	date1=$(date +"%s")
        hadoop jar $home/target/*.jar Iteration $initial $unordered 2>PR_iteration.out
	date2=$(date +"%s")
        diff=$(($date2-$date1))
	echo "* Took $diff seconds"
	echo "*" $(cat PR_iteration.out | grep Launched| awk '{print $6 $7 }')
	hdfs dfs -rm -r $initial &>/dev/null
	
	###### 	Ordering Output ########
	bytes=$(hls $unordered/part* | awk '{print $5}')
	mb=$(( bytes/1024/1024 ))
	echo PR vector Ordering for $mb MB
	date1=$(date +"%s")
        hadoop jar $home/target/*.jar PageOrderer $unordered $hdfs_output 2>PR_ordering.out
	date2=$(date +"%s")
        diff=$(($date2-$date1))
	echo "* Took $diff seconds"
	echo "*" $(cat PR_ordering.out | grep Launched| awk '{print $6 $7 }')
	hdfs dfs -rm -r $unordered &>/dev/null
	

 done

