export HADOOP_INSTALL=/opt/hadoop-2.7.0
export PATH=$PATH:$HADOOP_INSTALL/bin

input=$1
output=$2



hdfs dfs -copyToLocal $input tfidf_input >> /dev/null
python tfidf_scikit.py tfidf_input/ tfidf_output
hdfs dfs -copyFromLocal tfidf_output $output
rm -rf tfidf_input tfidf_output
