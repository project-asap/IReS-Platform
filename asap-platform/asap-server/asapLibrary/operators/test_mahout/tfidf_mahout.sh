#!/bin/bash
export JAVA_HOME='/opt/jdk1.7.0_71/'
export HADOOP_HOME='/opt/hadoop-2.6.0'
input=$1
mahout="/opt/mahout-distribution-0.10.0/bin/mahout"
hadoop="/opt/hadoop-2.6.0/bin/hadoop"
hdfs="/opt/hadoop-2.6.0/bin/hdfs"

#workaround for a params problem
#hv=$(hadoop version | grep Hadoop | awk  '{print $2}')
#if [[ "$hv" > "2.5" ]]; then xmdummy="-xm sequential"; fi

xmdummy="-xm sequential"
chunk=1

seqfiles="sequence_files"
${hdfs} dfs -rmr $seqfiles
#remove any previous files
${hdfs} dfs -rm $output/* 2>/dev/null | wc -l | echo [PREP] Deleted $(cat) old sequence files

${mahout} seqdirectory -i $input -o $seqfiles -c UTF-8 -chunk $chunk $xmdummy -ow 

minTF=20

output=$2
maxDFpercent=40

echo "[STEP 2/4] Sequence to Sparse $seqfiles, $output"
${mahout} seq2sparse \
             -i $seqfiles -ow --chunkSize $chunk \
	     -Dmapred.child.ulimit=15728640 -Dmapred.child.java.opts=-Xmx5g \
             -o $output --maxDFPercent $maxDFpercent --namedVector --minSupport $minTF