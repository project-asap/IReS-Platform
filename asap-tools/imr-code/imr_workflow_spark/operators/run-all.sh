input_csv="hdfs://master:9000/user/root/IMR/imr_training_suffled_small.csv"
w2vmodel="hdfs://master:9000/user/root/IMR/inter/w2vmodel"
w2voutput="hdfs://master:9000/user/root/IMR/inter/w2vout"
lr_model="hdfs://master:9000/user/root/IMR/inter/lr_model"
predicted="hdfs://master:9000/user/root/IMR/inter/predicted"

hdfs dfs -rm -r IMR/inter
hdfs dfs -mkdir IMR/inter

#W2V Train Operator
./w2v_train.sh $input_csv $w2vmodel
#W2V Vectorize Operator
./vectorize_docs.sh $input_csv $w2vmodel $w2voutput
#Logistic Regression Operator
./lr_train.sh $w2voutput $lr_model
#Classifier
./classifier.sh $w2voutput $predicted $lr_model
