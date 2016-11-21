input_docs=$1
class_output=$2
lr_model=$3
categories=labels.json
category=1

spark-submit --py-files imr_tools.py imr_classification.py classify $input_docs \
        --output $class_output \
        --model  $lr_model \
        --labels $categories \
        --category $category
