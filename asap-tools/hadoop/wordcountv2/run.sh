echo Hello World, Bye World! >file01
echo Hello Hadoop, Goodbye to hadoop. >file02

hdfs dfs -mkdir /user &>>test.out
hdfs dfs -mkdir /user/$USERNAME &>>test.out
hdfs dfs -put file0* input/ &>>test.out
rm file01 file02
hdfs dfs -rm -r output

#run a MR job
echo M-R task START
date1=$(date +"%s")
hadoop jar target/*.jar Main &>test.out 

date2=$(date +"%s")
diff=$(($date2-$date1))
echo "task took $diff sec. Output/Error in 'test.out'"

./show.sh
