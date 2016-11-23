HOME=`eval echo ~$USER`
export SPARK_HOME=/opt/spark
export PATH=$PATH:$SPARK_HOME/bin

input_docs=$1
class_output=$2
lr_model=$3
categories=labels.json
category=1

python_path=/root/imr-code/imr_workflow_spark/operators/imr_classification.py
categories=/root/imr-code/imr_workflow_spark/operators/labels.json

#hdfs dfs -rm -r $class_output

spark-submit --executor-memory 512M --driver-memory 512M \
	--py-files imr_tools.py $python_path classify $input_docs \
        --output $class_output \
        --model  $lr_model \
        --labels $categories \
        --category $category
