source $(dirname $0)/common.sh


echo "[STEP 1/2] K-Means"
  mahout kmeans \
    -i ${WORK_DIR}/my_synthetic_seq \
    -c ${WORK_DIR}/what_is_this \
    -o ${WORK_DIR}/clustering_raw_output\
    -dm org.apache.mahout.common.distance.CosineDistanceMeasure \
    -x ${max_iterations} -k ${K} -ow --clustering  &>step1.out
check step1.out

echo "[STEP 2/2] Clusterdump"
  mahout clusterdump \
    -i ${WORK_DIR}/clustering_raw_output/clusters-*-final \
    -o clusterdump_result \
    -dt sequencefile -b 100 -n 20 --evaluate -dm org.apache.mahout.common.distance.CosineDistanceMeasure -sp 0 \
    --pointsDir ${WORK_DIR}/clustering_raw_output/clusteredPoints &>step2.out


echo "[RESULT ]"

head clusterdump_result
rm clusterdump_result
hdfs dfs -rm -r ${WORK_DIR}
rm step*.out
