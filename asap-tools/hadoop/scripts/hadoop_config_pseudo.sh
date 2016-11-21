# script version : 2.0
# tested on hadoop version: 2.5.1 

#params
hadoop_dir=$(which hadoop | sed "s/\/bin.*//")
heap_size=2000
replication=1
zookeeper_dir="/etc/hadoop/data/zookeeper"
name_dir="$hadoop_dir/data/name_dir"
data_dir="$hadoop_dir/data/data_dir"
temp_dir="$hadoop_dir/data/temp_dir"

JAVA_HOME="\$(readlink \$(readlink \$(which java)) | sed \"s/bin\/java.*/bin/\")"
echo $JAVA_HOME
############### hadoop-env.sh #############################
hadoop_env="$hadoop_dir/etc/hadoop/hadoop-env.sh"
yarn_env="$hadoop_dir/etc/hadoop/yarn-env.sh"
#java home ( escaped /)
sed -i  "/export JAVA_HOME.*/d" $hadoop_env
sed -i "1s/^/export JAVA_HOME=$JAVA_HOME\n/" $hadoop_env
cat $hadoop_env | grep JAVA_HOME
exit

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
	echo PATH="$PATH" > ~/.bahsrc
fi

################ core-site.xml ###############################
cat <<EOF > "$hadoop_dir/etc/hadoop/core-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<!-- Put site-specific property overrides in this file. -->

<configuration>
<property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
</property>
<property>
	<name>hadoop.tmp.dir</name>
	<value>$temp_dir</value>
</property>
<property>
	<name>io.compression.codecs</name>
	<value>org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.BZip2Codec,org.apache.hadoop.io.compress.SnappyCodec
	</value>
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


###############  mapred-site  #####################
cat <<EOF > "$hadoop_dir/etc/hadoop/mapred-site.xml"
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <property>
    <name>mapred.job.tracker</name>
    <value>localhost:9001</value>
    </property>
</configuration>
EOF


############# format  ################
rm -rf $name_dir $data_dir $temp_dir
hdfs namenode -format
