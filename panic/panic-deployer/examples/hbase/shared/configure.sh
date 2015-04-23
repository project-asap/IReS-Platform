#!/bin/bash

# This scripts downloads and configures Hadoop.

JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"
HADOOP_URL="http://apache.cc.uoc.gr/hadoop/common/stable1/hadoop-1.2.1-bin.tar.gz"
HBASE_URL="http://apache.cc.uoc.gr/hbase/stable/hbase-0.98.3-hadoop1-bin.tar.gz"
HADOOP_INSTALLATION_PATH="/opt/hadoop"
HBASE_INSTALLATION_DIR="/opt/hbase"

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

download_hbase() {
echo "Downloading Hbase"
wget $HBASE_URL -O /tmp/hbase.tar.gz -o /tmp/hbase_wget_logs.txt
tar xfz /tmp/hbase.tar.gz -C /tmp/
mv /tmp/hbase-0.98.3-hadoop1/ $HBASE_INSTALLATION_DIR
echo "export PATH=\$PATH:/opt/hbase/bin/" >> /etc/profile
rm /tmp/hbase.tar.gz
echo "Hbase downloaded and extracted"
}

conf_xml(){
sed -i -e "/<configuration>/a \ \t<property>\n\t\t<name>$1</name>\n\t\t<value>$2</value>\n\t</property>" $3
}

configure_hadoop(){
NUMBER_OF_CORES=$(cat /proc/cpuinfo | grep processor | wc -l)
MEMORY_MB=$(free -m | grep -i mem | awk '{print $2}')

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

echo "Hadoop configured"
}

configure_hbase(){
# configure hbase-env.sh
MEMORY=$(free -m | grep -i mem | awk {'print $2'})
sed  -i 's|# export JAVA_HOME.*|export JAVA_HOME='"$JAVA_HOME"'|' $HBASE_INSTALLATION_DIR/conf/hbase-env.sh
sed  -i 's|# export HBASE_MANAGES_ZK=true|export HBASE_MANAGES_ZK=true|' $HBASE_INSTALLATION_DIR/conf/hbase-env.sh
sed  -i "s|# export HBASE_HEAPSIZE=1000|export HBASE_HEAPSIZE=$MEMORY|" $HBASE_INSTALLATION_DIR/conf/hbase-env.sh

#set regionservers
echo -n > $HBASE_INSTALLATION_DIR/conf/regionservers
SLAVES=$(cat /etc/hosts | grep slave | wc -l)
for i in $(seq 1 $SLAVES); do
  echo slave$i >> $HBASE_INSTALLATION_DIR/conf/regionservers
done

# configure hbase-site.xml
conf_xml hbase.rootdir hdfs://master1:9000/hbase $HBASE_INSTALLATION_DIR/conf/hbase-site.xml
conf_xml hbase.cluster.distributed true $HBASE_INSTALLATION_DIR/conf/hbase-site.xml
conf_xml hbase.zookeeper.property.clientPort 2222 $HBASE_INSTALLATION_DIR/conf/hbase-site.xml
conf_xml hbase.zookeeper.quorum master1 $HBASE_INSTALLATION_DIR/conf/hbase-site.xml
conf_xml hbase.zookeeper.property.dataDir /opt/zookeeper $HBASE_INSTALLATION_DIR/conf/hbase-site.xml

echo "Hbase configured"
}

download_hadoop
download_hbase
configure_hadoop
configure_hbase