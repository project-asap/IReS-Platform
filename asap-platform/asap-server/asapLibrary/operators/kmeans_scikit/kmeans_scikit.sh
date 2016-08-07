export HADOOP_INSTALL=/opt/hadoop-2.7.0
export PATH=$PATH:$HADOOP_INSTALL/bin

input=$1
output=$2
k=$3


hdfs dfs -copyToLocal $input kmeans_input >> /dev/null
python kmeans_scikit.py kmeans_input kmeans_output $k
hdfs dfs -copyFromLocal kmeans_output $output
rm -rf kmeans_input kmeans_output
