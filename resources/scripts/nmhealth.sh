#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 20/ 01/ 2016
Previous updates	: 26/ 10/ 2015
Host System			: Ubuntu >= 10.04 LTS
Hadoop				: 2.x
INFO

<<DESCRIPTION
A script running periodically to confirm the cluster health i.e. that all services
are running appropriately
DESCRIPTION

#import definitions
#. /home/hadoop/resources/experiments/definitions.sh

while read -r host services
do
	if [[ "$host" == "$HOSTNAME" ]]
	then
		echo -e "Host $host\nServices $services\n"
		#split the services into an array to check thereafter if they are running
		services=$(echo $services | tr "," "\n")
		for service in ${services[@]}
		do
			case "$service" in
			#database services
			"postgresql")
				status=`/etc/init.d/postgresql status | awk '{split( $0, a, " "); print a[ 4]}'`
				if [[ "$status" != "online" ]]
				then
					echo -e "ERROR: Postgresql is not running on '$HOSTNAME'."
					exit
				fi
				;;
			"mysql")
				status=`mysqladmin -u root -proot status | awk '{split( $0, a, ":"); print a[ 1]}'`
				if [[ "$status" != "Uptime" ]]
				then
					echo -e "ERROR: Mysql is not running on '$HOSTNAME'."
					exit
				fi
				;;
			#yarn cluster services			
			"DataNode")
				status=`jps | grep -w DataNode | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "DataNode" ]]
				then
					echo -e "ERROR: DataNode is not running at '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/hadoop-daemon.sh start datanode\n
to restart DataNode"
					exit
				fi
				;;			
			"NodeManager")
				status=`jps | grep -w NodeManager | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "NodeManager" ]]
				then
	        		echo -e "ERROR: NodeManager is not running at '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/yarn-daemon.sh start nodemanager\n
to restart NodeManager"
					exit
				fi
				;;			
			"NameNode")
				status=`jps | grep -w NameNode | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "NameNode" ]]
				then
					echo -e "ERROR: NameNode is not running on '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/hadoop-daemon.sh start namenode\n
to restart NameNode"
					exit
				fi
				;;			
			"ResourceManager")
				status=`jps | grep -w ResourceManager | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "ResourceManager" ]]
				then
					echo -e "ERROR: ResourceManager is not running on '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/yarn-daemon.sh start resourcemanager\n
to restart ResourceManager"
					exit
				fi
				;;			
			"SecondaryNameNode")
				status=`jps | grep -w SecondaryNameNode | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "SecondaryNameNode" ]]
				then
					echo -e "ERROR: SecondaryNameNode is not running on '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/hadoop-daemon.sh start secondarynamenode\n
to restart SecondaryNameNode"
					exit
				fi
				;;			
			"JobHistoryServer")
				status=`jps | grep -w JobHistoryServer | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "JobHistoryServer" ]]
				then
					echo -e "ERROR: JobHistoryServer is not running on '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	yarn/sbin/mr-jobhistory-daemon.sh start historyserver\n
to restart JobHistoryServer"
					exit
				fi
				;;		
			"Main")
				status=`jps | grep -w Main | awk '{split( $0, a, " "); print a[ 2]}'`
				if [[ "$status" != "Main" ]]
				then
					echo -e "ERROR: Asap server is not running on '$HOSTNAME'!\nFrom '$HOSTNAME' run\n
	 \$IRES_HOME/asap-platform/asap-server start\n
to restart Asap server"
					exit
				fi
				;;			
			*)
				#wrong service has been specified
				echo -e "ERROR: Wrong service $service provided to check on '$HOSTNAME'"
				exit
				;;
			esac
		done
	fi
done < services

echo -e "$HOSTNAME is healthy i.e. all necessary services are running"
