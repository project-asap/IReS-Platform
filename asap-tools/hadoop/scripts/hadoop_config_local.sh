# script version : 2.0
# tested on hadoop version: 2.5.1 

#params
hadoop_dir=/etc/hadoop
JAVA_HOME="\/usr\/lib\/jvm\/java-7-oracle\/jre\/" #escaped /
heap_size=2000
replication=1
zookeeper_dir="/etc/hadoop/data/zookeeper"
name_dir="$hadoop_dir/data/name_dir"
data_dir="$hadoop_dir/data/data_dir"
temp_dir="$hadoop_dir/data/temp_dir"

############### hadoop-env.sh #############################
hadoop_env="$hadoop_dir/etc/hadoop/hadoop-env.sh"
yarn_env="$hadoop_dir/etc/hadoop/yarn-env.sh"
#java home ( escaped /)
sed -i  "s/export JAVA_HOME.*/export JAVA_HOME=$JAVA_HOME/g" $hadoop_env
#heap size
sed -i "s/.*HADOOP_HEAPSIZE=.*/HADOOP_HEAPSIZE=$heap_size/g" $hadoop_env
sed -i "s/#.*YARN_HEAPSIZE.*/YARN_HEAPSIZE=${heapsize}/g" $yarn_env

############# PATH integration  ##########################
if [[ $(hadoop 2>&1 >/dev/null) =~ .*"not found".* ]]; then 
	export PATH="$PATH:$hadoop_dir/bin:$hadoop_dir/sbin";
	echo PATH="$PATH" >> ~/.bahsrc
fi

################ core-site.xml ###############################
cat <<EOF > "$hadoop_dir/etc/hadoop/core-site.xml"
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
</configuration>
EOF

##################  hdfs-site.xml  ########################
cat <<EOF > "$hadoop_dir/etc/hadoop/hdfs-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
</configuration>
EOF


###############  mapred-site  #####################
cat <<EOF > "$hadoop_dir/etc/hadoop/mapred-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
</configuration>
EOF


############# format  ################
rm -rf $name_dir $data_dir $temp_dir
hdfs namenode -format
