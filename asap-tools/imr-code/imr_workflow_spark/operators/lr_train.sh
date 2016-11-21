train_data=$1
lr_model=$2
categories=labels.json
category=1

# (2.1) create an initial model 
spark-submit --py-files imr_tools.py, \
        imr_classification.py train $train_data \
        --model  $lr_model \
        --labels $categories \
        --category $category \
        #--evaluate 
