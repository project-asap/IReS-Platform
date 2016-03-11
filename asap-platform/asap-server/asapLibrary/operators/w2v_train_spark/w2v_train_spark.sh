export SPARK_HOME=/opt/spark
export PATH=$PATH:/$SPARK_HOME/bin

echo "HELLO WORLD!"

csv_dataset=$1

w2v_model=$2


w2v_jar_path=/root/imr-code/imr_workflow_spark/operators/imr_w2v_2.11-1.0.jar # path for the w2v jar file

spark-submit --conf spark.eventLog.enabled=true --executor-memory 1G --driver-memory 1G $w2v_jar_path sm $csv_dataset hdfs://master:9000$w2v_model 
