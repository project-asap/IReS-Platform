# script version : 2.1
# tested on hadoop version: 2.5.1 

if [[ $# -ne 1 ]]; then
	echo Need parameter master or slave
fi

#params
hadoop_dir=/root/hadoop
JAVA_HOME="\/usr\/lib\/jvm\/java-7-oracle\/jre\/" #escaped /
heap_size=2000
replication=1
zookeeper_dir="/home/root/data/zookeeper"
name_dir="/home/root/data/name_dir"
data_dir="/home/root/data/data_dir"

############### hadoop-env.sh #############################
hadoop_env="$hadoop_dir/etc/hadoop/hadoop-env.sh"
yarn_env="$hadoop_dir/etc/hadoop/yarn-env.sh"
#java home ( escaped /)
sed -i  "s/export JAVA_HOME.*/export JAVA_HOME=$JAVA_HOME/g" $hadoop_env
#heap size
sed -i "s/HADOOP_HEAPSIZE=.*/HADOOP_HEAPSIZE=$heap_size/g" $hadoop_env
sed -i "s/#.*YARN_HEAPSIZE.*/YARN_HEAPSIZE=${heapsize}/g" $yarn_env
#parallel GC
gctest=$(cat $hadoop_env | grep ParallelGC | wc -l)
if [ $gctest = 0 ]; then
	echo export HADOOP_NAMENODE_OPTS=\"-XX:+UseParallelGC \${HADOOP_NAMENODE_OPTS}\" >>$hadoop_env
fi

############# PATH integration  ##########################
if [[ $(hadoop 2>&1 >/dev/null) =~ .*"not found".* ]]; then 
	export PATH="$PATH:$hadoop_dir/bin:$hadoop_dir/sbin";
	echo PATH="$PATH" > /etc/environment
fi

################ core-site.xml ###############################
cat <<EOF > "$hadoop_dir/etc/hadoop/core-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<!-- Put site-specific property overrides in this file. -->

<configuration>GL
<property>
  <name>hadoop.tmp.dir</name>
  <value>/tmp/hadoop_tmp_dir</value>
</property>
<property>
  <name>fs.defaultFS</name>
  <value>hdfs://master:9000</value>
</property>
<property>
  <name>io.compression.codecs</name>
  <value>org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.BZip2Codec,org.apache.hadoop.io.compress.SnappyCodec</value>
</property>
</configuration>
EOF

##################  hdfs-site.xml  ########################
cat <<EOF > "$hadoop_dir/etc/hadoop/hdfs-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
      <property>
        <name>dfs.datanode.max.xcievers</name>
        <value>4096</value>
      </property>
	<property>
		<name>dfs.replication</name>
		<value>$replication</value>
	</property>
        <property>
                <name>dfs.block.size</name>
		<!--<value>268435456</value>-->
		<value>67108864</value>
        </property>
	<property>
		<name>dfs.name.dir</name>
		<value>$name_dir</value>
	</property>
	<property>
		<name>dfs.data.dir</name>
		<value>$data_dir</value>
	</property>
	<property>
                <name>dfs.support.append</name>
                <value>true</value>
        </property>
        <property>
                <name>dfs.socket.timeout</name>
                <value>630000</value>
        </property>
        <property>
                <name>dfs.datanode.socket.write.timeout</name>
                <value>630000</value>
        </property>
</configuration>
EOF


if [[ $1 = master ]]; then
	############# format  ################
	rm -rf $name_dir $data_dir $temp_dir	
	hadoop namenode -format
fi
