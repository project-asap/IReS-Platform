HOME=`eval echo ~$USER`
TRAINPATH=/root/imr-code/web-analytics/operators

dataset=$1
lr_model=$2
param="{'loss':'log','n_iter':100,'alpha':0.0001}"

# Step 2.1 train the classifier
echo ======\> Train Classifier
echo "PWD $pwd"
python $TRAINPATH/train.py --trainFile $dataset --modelFile $lr_model --batchSize 1000 --classParam $param
