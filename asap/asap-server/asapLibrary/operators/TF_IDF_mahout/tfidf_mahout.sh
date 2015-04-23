#!/bin/bash
export JAVA_HOME='/opt/jdk1.7.0_71/'
input=$1
mahout="/opt/mahout-distribution-0.9/bin/mahout"
hadoop="/opt/hadoop-2.6.0/bin/hadoop"
hdfs="/opt/hadoop-2.6.0/bin/hdfs"

#workaround for a params problem
#hv=$(hadoop version | grep Hadoop | awk  '{print $2}')
#if [[ "$hv" > "2.5" ]]; then xmdummy="-xm sequential"; fi

xmdummy="-xm sequential"

seqfiles="sequence_files"
${hdfs} dfs -rmr $seqfiles
#remove any previous files
${hdfs} dfs -rm $output/* 2>/dev/null | wc -l | echo [PREP] Deleted $(cat) old sequence files

${mahout} seqdirectory -i $input -o $seqfiles -c UTF-8  $xmdummy -ow 

minTF=20

output=$2
maxDFpercent=40
chunk=64

echo "[STEP 2/4] Sequence to Sparse $seqfiles, $output"
${mahout} seq2sparse \
             -i $seqfiles --chunkSize $chunk\
             -o $output --maxDFPercent $maxDFpercent --namedVector --minSupport $minTF