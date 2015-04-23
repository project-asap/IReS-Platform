#!/bin/bash

# This scripts downloads and configures Hadoop.

JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"

HADOOP_URL="http://apache.cc.uoc.gr/hadoop/common/stable1/hadoop-1.2.1-bin.tar.gz"
HADOOP_INSTALLATION_PATH="/opt/hadoop"

HAMA_URL="http://mirror.cogentco.com/pub/apache/hama/hama-0.6.4/hama-0.6.4.tar.gz"
HAMA_INSTALLATION_PATH="/opt/hama/"

download_hadoop(){
echo "Downloading hadoop"
wget $HADOOP_URL -O /tmp/hadoop.tar.gz -o /tmp/wget_logs.txt
tar xfz /tmp/hadoop.tar.gz -C /tmp/
mv /tmp/hadoop-1.2.1/ $HADOOP_INSTALLATION_PATH
echo "export PATH=\$PATH:/opt/hadoop/bin/" >> /etc/profile
rm /tmp/hadoop.tar.gz

# add libsnappy to hadoo native lib

ln -s /usr/lib/libsnappy.so.1 /opt/hadoop/lib/native/Linux-amd64-64/libsnappy.so

echo "Hadoop downloaded and extracted"
}

download_hama(){
echo "Downloading hama"
wget $HAMA_URL -O /tmp/hama.tar.gz -o /tmp/hama_wget_logs.txt
tar xfz /tmp/hama.tar.gz -C /tmp/
mv /tmp/hama-*/ $HAMA_INSTALLATION_PATH
echo "export PATH=\$PATH:/opt/hama/bin/" >> /etc/profile
rm /tmp/hama.tar.gz

echo "Hama downloaded and extracted"

}


conf_xml(){
sed -i -e "/<configuration>/a \ \t<property>\n\t\t<name>$1</name>\n\t\t<value>$2</value>\n\t</property>" $3
}

configure_hadoop(){
NUMBER_OF_CORES=$(cat /proc/cpuinfo | grep processor | wc -l)
MEMORY_MB=$(free -m | grep -i mem | awk '{print$2}')

# set JAVA_HOME
sed  -i 's|# export JAVA_HOME.*|export JAVA_HOME='"$JAVA_HOME"'|' $HADOOP_INSTALLATION_PATH/conf/hadoop-env.sh

# set masters, slaves
echo "master1" > $HADOOP_INSTALLATION_PATH/conf/masters
echo -n > $HADOOP_INSTALLATION_PATH/conf/slaves
SLAVES=$(cat /etc/hosts | grep slave | wc -l)
for i in $(seq 1 $SLAVES); do 
  echo slave$i >> $HADOOP_INSTALLATION_PATH/conf/slaves
done

# configure core-site.xml
conf_xml fs.default.name hdfs://master1:9000 $HADOOP_INSTALLATION_PATH/conf/core-site.xml

# configure hdfs-site.xml
conf_xml dfs.replication 1 $HADOOP_INSTALLATION_PATH/conf/hdfs-site.xml
conf_xml dfs.name.dir /opt/hdfsname/ $HADOOP_INSTALLATION_PATH/conf/hdfs-site.xml
conf_xml dfs.data.dir /opt/hdfsdata/ $HADOOP_INSTALLATION_PATH/conf/hdfs-site.xml

# configure mapred-site.xml
conf_xml mapred.job.tracker master1:9001 $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml
conf_xml mapred.tasktracker.map.tasks.maximum $NUMBER_OF_CORES $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml
conf_xml mapred.tasktracker.reduce.tasks.maximum $NUMBER_OF_CORES $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml
conf_xml mapred.child.java.opts -Xmx${MEMORY_MB}m $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml
conf_xml mapred.compress.map.output true $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml
conf_xml mapred.map.output.compression.codec org.apache.hadoop.io.compress.SnappyCodec $HADOOP_INSTALLATION_PATH/conf/mapred-site.xml

echo "Hadoop configured"
}

configure_hama(){
# the master must ask the slave for cores
NUMBER_OF_CORES=$(ssh slave1 "cat /proc/cpuinfo | grep processor | wc -l")
MEMORY_MB=$(free -m | grep -i mem | awk '{print$2}')
SLAVES=$(cat /etc/hosts | grep slave | wc -l)

# set groomeservers
echo -n >$HAMA_INSTALLATION_PATH/conf/groomservers
for i in $(seq 1 $SLAVES); do
echo slave$i >> $HAMA_INSTALLATION_PATH/conf/groomservers
done

# configure hama-env.sh
sed  -i 's|# export JAVA_HOME.*|export JAVA_HOME='"$JAVA_HOME"'|' $HAMA_INSTALLATION_PATH/conf/hama-env.sh
sed  -i 's|# export HAMA_MANAGES_ZK=true|export HAMA_MANAGES_ZK=true|' $HAMA_INSTALLATION_PATH/conf/hama-env.sh

# configure hama-site.xml
conf_xml fs.default.name hdfs://master1:9000 $HAMA_INSTALLATION_PATH/conf/hama-site.xml
conf_xml hama.zookeeper.quorum master1,slave1,slave2 $HAMA_INSTALLATION_PATH/conf/hama-site.xml
conf_xml bsp.master.address master1:40000 $HAMA_INSTALLATION_PATH/conf/hama-site.xml
conf_xml bsp.tasks.maximum $NUMBER_OF_CORES $HAMA_INSTALLATION_PATH/conf/hama-site.xml
conf_xml bsp.child.java.opts -Xmx${MEMORY_MB}m $HAMA_INSTALLATION_PATH/conf/hama-site.xml


}

download_hadoop
download_hama
configure_hadoop
configure_hama