if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
fi
hadoop_link=http://apache.osuosl.org/hadoop/common/current/hadoop-2.6.0.tar.gz

hadoop_dir=$1

wget $hadoop_link -O hadoop.tar.gz
tar -xf hadoop.tar.gz
mv hadoop-* hadoop
mv hadoop $hadoop_dir
