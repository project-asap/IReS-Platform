#!/bin/bash
/opt/hadoop-2.7.0/bin/hdfs dfs -cat $1 | grep " Dog " >> $2
asap future-report -m cost=1
