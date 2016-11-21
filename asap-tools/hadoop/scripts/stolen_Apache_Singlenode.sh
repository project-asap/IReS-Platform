#!/bin/bash
###########################################################################################################################################
###########################################################################################################################################
############################################ Install Hadoop and set permissons ############################################################
###########################################################################################################################################
###########################################################################################################################################
################################################# Hive INSTALLATION #######################################################################
################################################ HBASE INSTALLATION #######################################################################
################################################# PIG INSTALLATION ########################################################################
###########################################################################################################################################
###########################################################################################################################################
echo "  "
echo "If you runing this script with bash you will not get any error. You are runing this script with sh. You will get this error"
echo 'If you get this error below "SingileNode_Ecosystem.sh: 16: SingileNode_Ecosystem.sh: source: not found"'
echo "  "
source /etc/environment
echo "  "
echo "  "

echo 'Just run this script with bash::: "bash SingileNode_Ecosystem.sh"'
echo "  "
echo "  "
echo 'run this command: "bash SingileNode_Ecosystem.sh"'
echo "  "

echo "Enter password for sudo user::"$cond
read cond
if [ ! -z "$cond" ]
then
a="/usr/bin/sudo -S"
na="echo $cond\n "
#$na  | $a cat /etc/sudoers
p=$(pwd)
b=$USER
$na  | $a rm -rf /usr/local/had
$na  | $a rm -rf /hadoop
$na  | $a apt-get update
$na  | $a apt-get upgrade -y
$na  | $a apt-get install openssh-server openssh-client -y
$na  | $a apt-get install openjdk-6-jdk openjdk-6-jre -y
$na  | $a mkdir /usr/local/had
$na  | $a mkdir /hadoop
$na  | $a chown $USER:$GROUP /hadoop
$na  | $a chown $USER:$GROUP /usr/local/had
echo "If you want chanage the hostname give your hostname what do you want..........."
echo "If you don't want to chanage the hostname just type enter key of key board....."
echo "                                                                               "
echo "Enter for skip install of Hostname:"$HOST
echo "Please give your Hostname:"$HOST
read HOST
if [  -z "$HOST" ]
then
echo Your hostname configuration successfully skiped..................
else
$na  | $a hostname $HOST
echo "$HOST" > a
$na  | $a mv a /etc/hostname
/sbin/ifconfig eth0 | grep 'inet addr' | cut -d':' -f2 | cut -d' ' -f1 > b
echo "$HOST" >> b
paste -s b > a
$na  | $a mv a /etc/hosts
echo Your hostname configuration successfully finced..................
rm b
fi
#### HADOOP HBASE HIVE PIG INSTALLATION ###
echo "Enter 0 for skip install of hadoop hive hbase pig::"$cond1
echo "Enter 1 for install of hadoop hive hbase pig::"$cond1
read cond1
echo "Enter 0 for skip install of auto strat hadoop when start the michen::"$cond2
echo "Enter 1 for install of auto strat hadoop when start the michen::"$cond2
read cond2
echo /usr/local/sbin > bin.list
echo /usr/local/bin >> bin.list
echo /usr/sbin >> bin.list
echo /usr/bin >> bin.list
echo /sbin >> bin.list
echo /bin >> bin.list
echo /usr/games >> bin.list
echo /usr/lib/jvm/java-6-openjdk-i386/bin >> bin.list
echo 'JAVA_HOME="/usr/lib/jvm/java-6-openjdk-i386"' > ho.list
echo Hadoop ecosystems Installation on single node started...............
if [ $cond1 -eq 0 ]
then
echo Hive install and configuration successfully skiped..................
echo Hadoop install and configuration successfully skiped................
echo Hbase install and configuration successfully skiped.................
echo Pig install and configuration successfully skiped...................

elif [ $cond1 -eq 1 ]
then
wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.9.3-bin.tar.gz
tar xzf apache-ant-1.9.3-bin.tar.gz
mv apache-ant-1.9.3 /usr/local/had/ant
echo /usr/local/had/ant/bin >> bin.list
notify-send 'The apache-ant Installation finced it will start installation of apache-maven' 'Do not distrub the  install of script'
wget  http://archive.apache.org/dist/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz
tar xzf apache-maven-3.1.1-bin.tar.gz
mv apache-maven-3.1.1 /usr/local/had/maven
echo /usr/local/had/maven/bin >> bin.list
notify-send 'The apache-maven Installation finced it will start installation of apache-forrest' 'Do not distrub the  install of script'
wget http://archive.apache.org/dist/forrest/0.9/apache-forrest-0.9.tar.gz
tar xzf apache-forrest-0.9.tar.gz
mv apache-forrest-0.9 /usr/local/had/forrest
echo /usr/local/had/forrest/bin >> bin.list
notify-send 'The apache-forrest Installation finced it will start installation of apache-hive' 'Do not distrub the  install of script'

wget http://archive.apache.org/dist/hive/stable/hive-0.11.0.tar.gz
tar xzf hive-0.11.0.tar.gz
mv hive-0.11.0 hive
echo "create database mshive" | mysql -u root -p'root'
cp hive/conf/hive-default.xml.template hive/conf/hive-default.xml
cp hive/conf/hive-env.sh.template hive/conf/hive-env.sh
cp hive/conf/hive-log4j.properties.template hive/conf/hive-log4j.properties
cp mysql-connector-java-5.1.18-bin.jar hive/lib
sed 's/#\ export\ HIVE_CONF_DIR=/export\ HIVE_CONF_DIR=\"\/usr\/local\/hadoop\/hive\/conf\"/g' hive/conf/hive-env.sh -i
sed 's/#\ export\ HIVE_AUX_JARS_PATH=/export\ HIVE_AUX_JARS_PATH=\"\/usr\/local\/hadoop\/hive\/lib\/mysql-connector-java-5.1.25-bin.jar\"/g'  hive/conf/hive-env.sh -i
sed 's/<value>jdbc:derby:;databaseName=metastore_db;create=true<\/value>/<value>jdbc:mysql:\/\/localhost:3306\/myshive?createDatabaseIfNotExist=true<\/value>/g' hive/conf/hive-default.xml -i
sed 's/<value>APP<\/value>/<value>root<\/value>/g' hive/conf/hive-default.xml -i
sed 's/<value>mine<\/value>/<value>root<\/value>/g' hive/conf/hive-default.xml -i
sed 's/org.apache.derby.jdbc.EmbeddedDriver/com.mysql.jdbc.Driver/g' hive/conf/hive-default.xml -i
mv hive /usr/local/had
echo /usr/local/had/hive/bin >> bin.list
echo 'HIVE_HOME="/usr/local/had/hive"' >> ho.list
echo 'HIVE_CONF_DIR="/usr/local/had/hive/conf"' >> ho.list
echo Hive install and configuration successfully over....................
wget http://hortonworks.com/wp-content/uploads/2013/05/hive_logo.png
p=$(pwd)
icon="$p/hive_logo.png"
notify-send -i $icon 'The apache-hive Installation finced' 'Do not distrub the script it run another install'

wget http://archive.apache.org/dist/hadoop/common/stable1/hadoop-1.2.1.tar.gz
tar xzf hadoop-1.2.1.tar.gz
mv hadoop-1.2.1 hadoop
sed "s/<\/configuration>/<property>\n<name>hadoop.tmp.dir<\/name>\n<value>\/hdoop<\/value>\n<description>a base for other temporary directories<\/description>\n<\/property>\n<property>\n<name>fs.default.name<\/name>\n<value>hdfs:\/\/$c:54310<\/value>\n<\description>location of name node<\/description>\n<\/property>\n<\/configuration>/g" -i.bak hadoop/conf/core-site.xml
sed 's=<configuration>=<configuration>\n<property>\n<name>mapred.job.tracker<\/name>\n<value>'$c':54311<\/value>\n<\/property>\n<property>\n<name>mapred.child.java.opts<\/name>\n<value>-Xmx512m<\/value>\n<\/property>=g' -i.bak hadoop/conf/mapred-site.xml
sed 's=<configuration>=<configuration>\n<property>\n<name>dfs.replication<\/name>\n<value>1<\/value>\n<description>default block replication<\/description>\n<\/property>=g' -i.bak hadoop/conf/hdfs-site.xml
sed 's/localhost/'$c'/g' -i.bak hadoop/conf/slaves
sed 's/localhost/'$c'/g' -i.bak hadoop/conf/masters
mv hadoop /usr/local/had
echo /usr/local/had/hadoop/bin >> bin.list

echo 'HADOOP_HOME="/usr/local/had/hadoop"' >> ho.list
echo 'HADOOP_CONF_DIR="/usr/local/had/hadoop/conf"' >> ho.list
echo Hadoop install and configuration successfully over..................
wget http://www.parallelx.com/img/hadoop-elephant_logo.png
wget http://cloudtimes.org/wp-content/uploads/2013/06/hadoop-logo-square.jpg
p=$(pwd)
icon="$p/hadoop-logo-square.jpg"
notify-send -i $icon 'The apache-hadoop Install finced' 'Do not distrub the script it run another install'

wget http://archive.apache.org/dist/hbase/stable/hbase-0.94.18.tar.gz
tar xzf hbase-0.94.18.tar.gz
mv hbase-0.94.16 hbase
sed "s/<\/configuration>/<property>\n<name>hbase.rootdir<\/name>\n<value>hdfs:\/\/'$c':54310\/hbase<\/value>\n<\/property>\n<property>\n<name>hbase.cluster.distributed<\/name>\n<value>true<\/value>\n<\/property>\n<property>\n<name>hbase.zookeeper.property.clientPort<\/name>\n<value>2181<\/value>\n<\/property>\n<property>\n<name>hbase.zookeeper.quorum<\/name>\n<value>'$c'<\/value>\n<\/property>\n<\/configuration>/g" -i.bak hbase/conf/hbase-site.xml
sed 's/localhost/'$c'/g' hbase/conf/regionservers -i
sed 's/#\ export\ HBASE_MANAGES_ZK=true/export\ HBASE_MANAGES_ZK=true/g' hbase/conf/hbase-env.sh -i
mv hbase /usr/local/had/
echo /usr/local/had/hbase/bin >> bin.list
echo 'HBASE_HOME="/usr/local/had/hbase"' >> ho.list
echo 'HBASE_CONF_DIR="/usr/local/had/hbase/conf"' >> ho.list
echo Hbase install and configuration successfully over...................
wget http://www.bigsql.org/se/images/hbase.png
p=$(pwd)
icon="$p/hbase.png"
notify-send -i $icon 'The apache-hbase Install finced' 'Do not distrub the script it run another install'

wget http://archive.apache.org/dist/pig/stable/pig-0.12.0.tar.gz
tar xzf pig-0.12.0.tar.gz
cd pig-0.12.0
ant
cd -
mv pig-0.12.0 /usr/local/had/pig
echo /usr/local/had/pig/bin >> bin.list
echo 'PIG_HOME="/usr/local/had/pig"' >> ho.list
echo Pig install and configuration successfully over.....................
wget http://joshualande.com/assets/pig_logo.jpg
p=$(pwd)
icon="$p/pig_logo.jpg"
notify-send -i $icon 'The apache-pig Install finced' 'Do not distrub the script it run another install'
fi

if [ $cond2 -eq 0 ]
then
cat bin.list | paste -s | sed 's/\t/:/g' | sed 's/^/"/g' | sed 's/^/=/g' | sed 's/^/PATH/g' | sed 's/$/"/g' > en
cat ho.list >> en
$na  | $a mv en /etc/environment
source /etc/environment
elif [ $cond2 -eq 1 ]
then
cat bin.list | paste -s | sed 's/\t/:/g' | sed 's/^/"/g' | sed 's/^/=/g' | sed 's/^/PATH/g' | sed 's/$/"/g' > en
cat ho.list >> en
$na  | $a mv en /etc/environment
source /etc/environment
a=$USER
b=$(hostname)
e=$(which start-all.sh)
f=$(which hadoop)
g=$(which start-hbase.sh)
h=$(which zkServer.sh)
echo '#!/bin/sh' > st.sh
echo 'b=$(hostname)' >> st.sh
echo 'c=$(ifconfig eth0 | grep "inet addr" | cut -d':' -f2 | cut -d' ' -f1 )' >> st.sh
echo 'echo $c $b > /etc/hosts' >> st.sh
echo "ssh $a@$b $f" >> st.sh
echo "ssh $a@$b $g" >> st.sh
echo "ssh $a@$b $h start" >> st.sh
chmod +x st.sh
$na  | $a mkdir /root/cron
$na  | $a mv st.sh /root/cron/st.sh
$na  | $a cp /var/spool/cron/crontabs/root .
$na  | $a chown $a root
echo '@reboot sleep && 30 /root/cron/st.sh >> /root/cron/alst.log 2>&1' >> root
$na  | $a chown root:root root
$na  | $a chmod 0600 root
$na  | $a mv root /var/spool/cron/crontabs/
echo Hosts configuration successfully over...............................
else
echo $cond
fi
fi
