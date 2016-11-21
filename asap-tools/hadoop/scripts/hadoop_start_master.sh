#remove logs
rm -rf /root/hadoop/logs/*

#start namenode
hadoop-daemon.sh --script hdfs start namenode

#start YARN resource manager (on master)
yarn-daemon.sh start resourcemanager

#start YARN node manager (on nodes)
yarn-daemon.sh start nodemanager

#start datanode
hadoop-daemon.sh --script hdfs start datanode
