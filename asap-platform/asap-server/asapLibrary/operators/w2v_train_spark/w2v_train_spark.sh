export SPARK_HOME=/opt/spark
export PATH=$PATH:/$SPARK_HOME/bin
export NUM_CORES=$3
export MEMORY=$4

echo "HELLO WORLD!"

csv_dataset=$1

w2v_model=$2

w2v_jar_path=/root/imr-code/imr_workflow_spark/operators/imr_w2v_2.11-1.0.jar

echo "PARAMS: ${w2v_jar_path} ${w2v_model}"

spark-submit --executor-memory ${MEMORY%.*}M --driver-memory 1G --total-executor-cores ${NUM_CORES%.*} $w2v_jar_path sm $csv_dataset hdfs://master:9000$w2v_model 
