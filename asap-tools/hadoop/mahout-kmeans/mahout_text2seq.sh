source $(dirname $0)/common.sh

input=$1

#workaround for a params problem
hv=$(hadoop version | grep Hadoop | awk  '{print $2}')
if [[ "$hv" > "2.5" ]]; then xmdummy="-xm sequential"; fi

output=${WORK_DIR}/sequence_files

#remove any previous files
hdfs dfs -rm $output/* 2>/dev/null | wc -l | echo [PREP] Deleted $(cat) old sequence files

mahout seqdirectory -i ${input} -o $output -c UTF-8 -chunk $chunk  $xmdummy -ow &>step1.out
check step1.out
