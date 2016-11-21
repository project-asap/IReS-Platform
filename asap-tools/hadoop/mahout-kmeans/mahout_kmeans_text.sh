source $(dirname $0)/common.sh

input=$1 
K=$2
max_iterations=$3
input=$1
raw_output=$4

#remove anything in output
hdfs dfs -rm -r $raw_output/* 2>/dev/null


mahout kmeans \
	-Dmapred.child.ulimit=15728640 -Dmapred.child.java.opts=-Xmx6g \
	-Dmapred.map.tasks=4 -Dmapred.max.split.size=$((chunk*1024*1024/4)) \
	-i ${input}/tfidf-vectors/ \
	-o ${raw_output} \
	-dm org.apache.mahout.common.distance.CosineDistanceMeasure \
	-x ${max_iterations} \
	-k ${K}\
	-ow --clustering \
	-c /tmp/mahout_rand_clusters \
	&>step3.out 

check step3.out



#little hack - investigate further
final_clusters=$(hdfs dfs -ls $raw_output | grep final | awk '{print $8}')
echo $final_clusters

echo "[STEP 4/4] Clusterdump"
  mahout clusterdump \
    -i ${final_clusters} \
    -o clusterdump_result.out \
    -d ${input}/dictionary.file-0 \
    -dt sequencefile -b 100 -n 20 --evaluate -dm org.apache.mahout.common.distance.CosineDistanceMeasure -sp 0 \
    --pointsDir ${raw_output}/clusteredPoints &>step4.out
check step4.out

echo "[RESULT  ]"
 head clusterdump_result
rm clusterdump_result
