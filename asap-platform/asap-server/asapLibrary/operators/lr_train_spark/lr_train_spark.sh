HOME=`eval echo ~$USER`
export SPARK_HOME=/opt/spark
export PATH=$PATH:$SPARK_HOME/bin

train_data=$1
lr_model=$2
python_path=/root/imr-code/imr_workflow_spark/operators/imr_classification.py
categories=/root/imr-code/imr_workflow_spark/operators/labels.json
category=1

# (2.1) create an initial model 
spark-submit \
	--executor-memory 512M \
	--driver-memory 512M \
        --py-files imr_tools.py, \
        $python_path train $train_data \
        --model  $lr_model \
        --labels $categories \
        --category $category
