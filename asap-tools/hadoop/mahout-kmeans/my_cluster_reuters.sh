
output=output
input=input

WORK_DIR=/tmp/kmeans_reuters

echo "creating work directory at ${WORK_DIR}"
hdfs dfs -rm -r $WORK_DIR; 
hdfs dfs -mkdir -p ${WORK_DIR}/text_files

check (){
	e=$( cat $1 | grep Exception)
	t=$( echo $e | wc -c)
	if [ "$e" != "" ]; then
		echo $e
		exit
	fi
}


echo Extracting
mkdir reuters-sgm
tar xzf reuters21578.tar.gz -C reuters-sgm

echo "[STEP 1.1/4] Extracting text from Reuters .sgm files with Lucene (local)"
#ths happens locally
mahout org.apache.lucene.benchmark.utils.ExtractReuters reuters-sgm text_files_tmp &>step1.out
check step1.out

echo "[STEP 1.2/4] Putting files to HDFS"
hdfs dfs -put text_files_tmp/* ${WORK_DIR}/text_files
#rm -r text_files_tmp		

echo "[STEP 1.3/4] Converting to Sequence Files from Directory"
mahout seqdirectory -i ${WORK_DIR}/text_files -o ${WORK_DIR}/sequence_files -c UTF-8 -chunk 64  -xm sequential -ow &>step1.out
check step1.out


echo "[STEP   2/4] Sequence to Sparse"
 mahout seq2sparse \
    -i ${WORK_DIR}/sequence_files \
    -o ${WORK_DIR}/sparce_matrix_files --maxDFPercent 85 --namedVector &>step2.out
check step2.out

echo "[STEP   3/4] K-Means"
  mahout kmeans \
    -i ${WORK_DIR}/sparce_matrix_files/tfidf-vectors/ \
    -c ${WORK_DIR}/what_is_this \
    -o ${WORK_DIR}/clustering_raw_output\
    -dm org.apache.mahout.common.distance.CosineDistanceMeasure \
    -x 10 -k 20 -ow --clustering  &>step3.out
check step3.out

echo "[STEP   4/4] Clusterdump"
  mahout clusterdump \
    -i ${WORK_DIR}/clustering_raw_output/clusters-*-final \
    clusterdump_result \
    -d ${WORK_DIR}/sparce_matrix_files/dictionary.file-0 \
    -dt sequencefile -b 100 -n 20 --evaluate -dm org.apache.mahout.common.distance.CosineDistanceMeasure -sp 0 \
    --pointsDir ${WORK_DIR}/clustering_raw_output/clusteredPoints &>step4.out
check step4.out

echo "[RESULT    ]"
 head clusterdump_result
 rm clusterdump_result
