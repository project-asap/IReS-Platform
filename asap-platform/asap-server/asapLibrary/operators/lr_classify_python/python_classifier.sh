HOME=`eval echo ~$USER`

data_dir=$1
model=$2

python_path=/root/imr-code/web-analytics/operators/predict.py

python $python_path --inputFile $data_dir/vectorized_data.txt --modelFile $model --batchSize 1000

