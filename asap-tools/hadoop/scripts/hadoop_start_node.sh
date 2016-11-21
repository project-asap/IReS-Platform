#remove logs
rm -rf /root/hadoop/logs/*

#start YARN node manager (on nodes)
yarn-daemon.sh start nodemanager

#start datanode
hadoop-daemon.sh --script hdfs start datanode
