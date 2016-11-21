#!/bin/bash
function get_slaves {
	asked=$1
	available=$(cat /opt/available_slaves | wc -l)
	if (( asked>available)); then
		echo ERROR: I do not have that many slaves available \(asked $asked, avail: $available\)
		exit -2
	fi

	cat /opt/available_slaves | head -n $1
}

function reconfigure_yarn( ){
	# expects as input the #no of yarn slaves to make available
	asked=$1

	# stop prev yarn configuration
	stopped=$($HADOOP_HOME/sbin/stop-yarn.sh | grep nodemanager | wc -l)
	echo -n "YARN Workers: Stopped $stopped"
	# set slaves file
	get_slaves $asked > $HADOOP_HOME/etc/hadoop/slaves
	# start yanr
	started=$($HADOOP_HOME/sbin/start-yarn.sh |grep nodemanager | wc -l)
	echo " Started $started"

}

function reconfigure_spark( ){
	# expects as input the #no of yarn slaves to make available
	asked=$1
	# stop prev spark configuration
	stopped=$($SPARK_HOME/sbin/stop-all.sh | grep worker.Worker | wc -l)
	echo -n "SPARK Workers: Stopped $stopped"
	# set slaves file
	get_slaves $asked > $SPARK_HOME/conf/slaves
	# start spark
	started=$($SPARK_HOME/sbin/start-all.sh |grep worker.Worker | wc -l)
	echo " Started $started"

}
