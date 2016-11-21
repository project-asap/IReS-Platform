rm test.out &>/dev/null

# DFS 
echo "STARTED test"

#create a dummy file
echo "troll troll troll trollolo dummy dummy">dummy.tmp

#put it do dfs
hdfs dfs -mkdir -p /user/$USERNAME/input
hdfs dfs -put dummy.tmp input/
rm dummy.tmp

#delete output from prev runs
hdfs dfs -rm -r output 

#run a MR job
echo STARTED M-R task
echo "TTTTTTTT  starting M-R  TTTTTTTTT" >>test.out
date1=$(date +"%s")
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.*.jar grep input output 'troll'  

date2=$(date +"%s")
diff=$(($date2-$date1))
echo "M-R took $diff sec. Output/Error in 'test.out'"
echo RESULT:
hdfs dfs -cat output/*
