# kafka conf files
function clear-kafka-data(){
	# kafka stream logs
	rm -f $KAFKA_HOME/logs/*; rm -rf /tmp/kafka-logs

	# zookeeper stream logs
	rm -f  /tmp/zookeeper/version-2/*

	# stdout/err logs
	rm -f /tmp/kafka_*.log
}


zoo_conf=$KAFKA_HOME/config/zookeeper.properties
kafka_svr_conf=$KAFKA_HOME/config/server.properties
topic=test
zoo_addr=localhost:2181 


#ignore
setopt no_rm_star_silent &>/dev/null


#start zookeeper
alias start-zookeeper="nohup $KAFKA_HOME/bin/zookeeper-server-start.sh $zoo_conf &> /tmp/kafka_zookeeper.log &"

#start kafka
alias start-kafka="nohup $KAFKA_HOME/bin/kafka-server-start.sh $kafka_svr_conf &> /tmp/kafka_server.log &"

#stop/kill kafka
alias stop-kafka="$KAFKA_HOME/bin/kafka-server-stop.sh"
#kill -9 $(jps | grep Kafka | awk '{print $1}')

#stop/kill zookeeper
alias stop-zookeeper="$KAFKA_HOME/bin/zookeeper-server-stop.sh"
#kill -9 $(jps | grep QuorumPeerMain | awk '{print $1}')

# create topic
alias create-kafka-topic="$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $zoo_addr --replication-factor 1 --partitions 1 --topic"

# delete topic # conf/server.properies -> delete.topic.enable=true
alias delete-kafka-topic="$KAFKA_HOME/bin/kafka-topics.sh --zookeeper $zoo_addr --delete --topic"

# list topics
alias list-kafka-topics="$KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper $zoo_addr"

# console producer
alias kafka-console-producer="$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic"

# console consumer
alias kafka-console-consumer="$KAFKA_HOME/bin/kafka-console-consumer.sh --zookeeper $zoo_addr --from-beginning --topic"

#send random data to topic
#dd if=/dev/urandom of=dummy.txt bs=204800 count=10 | bin/kafka-console-producer.sh --broker-list localhost:9092 --topic $topic
