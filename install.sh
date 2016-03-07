#!/bin/bash

<<INFO
Author				:	Papaioannou Vassiis
Last update			:	06/ 03/ 2016
Previous updates	:	04/ 03/ 2016
Platform			:	ASAP IReS
Github				:	https://github.com/project-asap/IReS-Platform
INFO

<<DESCRIPTION
This script configures ASAP server to run properly
DESCRIPTION

# ASAP server configuration process
# 1. Build panic, cloudera-kitten, asap-platform
# 2. Take home folder of YARN
# 2.1 Update asap.properties file
# 3. Take home folder of IRES
# 3.1 Update asap-server script
# 4. Take full host name or host ip which runs Hadoop YARN
# 4.1 Update YARN configuration files yarn-site.xml and core-site.xml
# 4.2 Copy updated files into asap-server/target/conf folder
declare -a modules=( panic cloudera-kitten asap-platform)
declare -a conf=()
configured=false
while ! $configured
do
	zenity	--info \
	 		--title="Welcome to ASAP server!" \
	 		--text="This is the first time configuration of server."

	# build ASAP server
	percentage=0
	for module in ${modules[@]}
	do
		cd $module
		# build in quiet mode i.e. print only errors
		mvn clean install -DskipTests -q > log.txt
		# check for errors
		errors=`cat log.txt | grep ERROR`
		if [ ! -z $errors ]
		then
			echo -e "$errors"
			zenity	--error \
			 		--title="List of errors found!" \
			 		--text="$errors\n\nContact administrator to fix them."
			exit
		fi
		# remove log.txt since everything went well
		rm log.txt
		cd ../
		percentage=$(( $percentage + 33))
		echo $percentage 
	done | zenity --progress --text="Building ASAP server ... please wait" --percentage=0 --auto-close
	# inform about builting success
	zenity	--info \
	 		--title="ASAP server builting" \
	 		--text="ASAP server was built successfully."
	
	# specify home folder of HADOOP YARN
	YARN_HOME=`zenity	--title="Specify home folder of Hadoop YARN" \
						--file-selection \
						--directory `
	# specify host name running of HADOOP YARN
	HOST_NAME=`zenity	--entry \
						--entry-text="Full host name or host ip" \
						--title="Specify the full name or the ip of host running Hadoop YARN" \
						--text="Specify the full name or the ip of host running Hadoop YARN"`
						
	# update YARN_HOME/etc/hadoop/yarn-site.xml with the properties of resources/conf/yarn-site-min.xml file
	# if the property yarn.resour.hostname exists in YARN_HOME/etc/hadoop/yarn-site.xml file
	# then this property will exist twice
	# update with the appropriate host name
	sed -i "s_master_"$HOST_NAME"_" resources/conf/yarn-site-min.xml
	#extract now just all the properties that reside inside <configuration></configuration> tags
	start=`sed -n '/<configuration>/=' resources/conf/yarn-site-min.xml`
	start=$(( $start + 1))
	end=`sed -n '/<\/configuration>/=' resources/conf/yarn-site-min.xml`
	end=$(( $end - 1))
	properties=`sed -n "$start","$end"p resources/conf/yarn-site-min.xml`
	# add minimum set of properties
	sed -i 's_<\/configuration>__' $YARN_HOME/etc/hadoop/yarn-site.xml
	echo -e "$properties\n</configuration>" >> $YARN_HOME/etc/hadoop/yarn-site.xml
	#extract now just all the properties that reside inside <configuration></configuration> tags
	start=`sed -n '/<configuration>/=' resources/conf/core-site-min.xml`
	start=$(( $start + 1))
	end=`sed -n '/<\/configuration>/=' resources/conf/core-site-min.xml`
	end=$(( $end - 1))
	properties=`sed -n "$start","$end"p resources/conf/core-site-min.xml`
	# add minimum set of properties
	sed -i 's_<\/configuration>__' $YARN_HOME/etc/hadoop/core-site.xml
	echo -e "$properties\n</configuration>" >> $YARN_HOME/etc/hadoop/core-site.xml

	# copy Hadoop YARN configuration files into ASAP server
	cp $YARN_HOME/etc/hadoop/yarn-site.xml asap-platform/asap-server/target/conf
	cp $YARN_HOME/etc/hadoop/core-site.xml asap-platform/asap-server/target/conf
	
	# IRES_HOME path is the current path
	IRES_HOME=`pwd`
	update asap-platform/asap-server/src/main/scripts/asap-server file
	sed -i "s:^IRES_HOME=.*:IRES_HOME="$IRES_HOME":" asap-platform/asap-server/src/main/scripts/asap-server
	
	# set asap.properties file
	# append
	# path of hdfs command of Hadoop YARN
	sed -i "s:^asap\.hdfs_path.*:asap\.hdfs_path ="$YARN_HOME"/bin/hdfs:" asap-platform/asap-server/src/main/resources/asap.properties
	# path of asap command of ASAP server which does the reporting( takine measures)
	sed -i "s:^asap\.asap_path.*:asap\.asap_path = /home/"$USER"/asap/bin/asap:" asap-platform/asap-server/src/main/resources/asap.properties
	
#	echo $HOST_NAME
#	echo $YARN_HOME
#	echo $IRES_HOME
	#write configuration properties into asap_conf file
	conf=( $YARN_HOME $IRES_HOME)
	configured=true
done
