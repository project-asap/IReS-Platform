JAVA_HOME="\/usr\/lib\/jvm\/java-7-oracle\/jre\/"
#####################################################

#set JAVA_HOME
sed -i "s/.*export JAVA_HOME.*/export JAVA_HOME=$JAVA_HOME/g" hbase/conf/hbase-env.sh

# hbase-site configuration
cat <<'EOF' > hbase/conf/hbase-site.xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
  <property>
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/user/root/data/zookeeper</value>
  </property>
<property>
  <name>hbase.cluster.distributed</name>
  <value>true</value>
</property>
<property>
  <name>hbase.rootdir</name>
  <value>hdfs://master:9000/hbase</value>
</property>
<property>
  <name>hbase.zookeeper.quorum</name>
  <value>master</value>
</property>
</configuration>
EOF



#only add hbase/bin to PATH if not already there
if [[ $(hbase 2>&1 >/dev/null) =~ .*"not found".* ]]; then export PATH="$PATH:/root/hbase/bin"; fi
echo PATH="$PATH" > /etc/environment
rm hbase/lib/slf4j-log4j12-1.6.4.jar 2>/dev/null

echo "master">hbase/conf/regionservers

#remove preivous logs
rm hbase/logs/* 2>/dev/null

#start hbase
hbase/bin/start-hbase.sh