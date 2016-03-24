#!/bin/bash
/opt/hadoop-2.7.0/bin/hdfs dfs -rm $2

#v=$[(100 + (RANDOM % 100))]$[1000 + (RANDOM % 1000)]
#echo $v
#v=0.${v:1:2}${v:4:3}
#echo $v

/opt/hadoop-2.7.0/bin/hdfs dfs -cat $1 | perl -sne 'print if (rand() < $r)' -- -r=$3  | /opt/hadoop-2.7.0/bin/hdfs dfs -put - $2
