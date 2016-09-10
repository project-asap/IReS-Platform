#!/bin/bash

<<INFO
Author				:	Papaioannou Vassiis
Last update			:	10/ 06/ 2016
Previous updates		:	06/03/2016, 04/ 03/ 2016
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
# 4.3 Build NodeManager health script i.e. resources/conf/yarnServices

#define project modules to build
#declare -a modules=( panic cloudera-kitten asap-platform)
declare -a modules=( panic cloudera-kitten asap-platform cloudera-kitten)
declare -a conf=()
#the installation process will be either via terminal or a GUI; installation through terminal
#i.e. command line is the default one
INSTALL_MODE="cli"
#derive IReS home folder
IRES_HOME=$0
if [[ $IRES_HOME == ./* ]]
then
	#case of ./.../install.sh
	IRES_HOME=`echo -e ${IRES_HOME#*./}`
	if [[ $IRES_HOME == "install.sh" ]]
	then
		IRES_HOME=""
	else
		IRES_HOME="/"`echo -e ${IRES_HOME%/install.sh}`
	fi
	IRES_HOME=`pwd`"$IRES_HOME"
else
	#case of ../../../..../install.sh
	rcd=""
	while [[ $IRES_HOME == ../* ]]
	do
		cd ../
		IRES_HOME=`echo -e ${IRES_HOME#*../}`
		rcd=$rcd"../"
	done
	if [[ $IRES_HOME == "install.sh" ]]
	then
		IRES_HOME=""
	else
		IRES_HOME="/"`echo -e ${IRES_HOME%/install.sh}`
	fi
	IRES_HOME=`pwd`$IRES_HOME
	cd $rcd
fi
echo -e "IReS-Platfrom home folder is $IRES_HOME"
#add some color
. $IRES_HOME/resources/textformat/colors.sh
#Hadoop YARN home folder
YARN_HOME=

usage()
{
	cat << EOF
	usage: $0 [OPTIONS]

	This script installs and builds IReS scheduler appropriately to your local system. By
	default a command line installation process is invoked.

	OPTIONS:
	
	-h
		show this message,
		
	-r command
		Start, stop or restart ASAP server. Command can be one of start, restart and stop.

	-s path 	
		set IReS home folder according to IReS local installation in order for the ASAP server
		to work correctly. The path should be the /absolute/path/to/IReS/home/folder.

	-c path1,path2
		connect ASAP server to a Hadoop YARN installation. ${bold}Path1${reset} should be the
		/absolute/path/to/Hadoop_YARN/home/folder. ${bold}Path2${reset} should be the local home
		folder of IReS. Both paths are required in the specified order.

	-l n,i
		view the last n lines of server logs. If i = 0, then logs will be opened in interactive mode
		starting from the last n lines. Usage examples,
		1. ./install.sh -l 15,0	        --> the last 15 log lines will be displayed in interactive mode
		2. ./install.sh -l 15	        --> the last 15 log lines will be displayed( no interactive mode)
		3. ./install.sh -l 0	        --> the default amount of log lines( 10) will be displayed in interactive mode
		4. ./install.sh -l 	        --> is not acceptable, at least one argument should be given
		
	-g
		enables graphical installation. This installation process needs ${bold}xserver${reset}
		to be installed and set appropriately for the user who runs this script. If command
		${bold}xeyes${reset} runs successfully then xserver is configured correctly for the
		user running the script,
		

EOF
}

setIresHome()
{
	# update asap-platform/asap-server/src/main/scripts/asap-server file
	sed -i "s:^IRES_HOME.*=.*:IRES_HOME=\"$IRES_HOME\":" $IRES_HOME/asap-platform/asap-server/src/main/scripts/asap-server
	# update asap.server_home variable in asap-platform/asap-server/src/main/resources/asap.properties file
	sed -i "s:^asap\.server_home.*:asap\.server_home = $IRES_HOME/asap-platform/asap-server/target:" $IRES_HOME/asap-platform/asap-server/src/main/resources/asap.properties
	sed -i "s:^IRES_HOME.*=.*:IRES_HOME=\"$IRES_HOME\":" $IRES_HOME/asap-platform/asap-server/asapLibrary/BasicLuaConf.lua
	sed -i "s:^IRES_HOME.*=.*:IRES_HOME=\"$IRES_HOME\":" $IRES_HOME/asap-platform/asap-server/defaultAsapLibrary/BasicLuaConf.lua
	if [[ -e $IRES_HOME/asap-platform/asap-server/target/asapLibrary/BasicLuaConf.lua ]]
	then
		sed -i "s:^IRES_HOME.*=.*:IRES_HOME=\"$IRES_HOME\":" $IRES_HOME/asap-platform/asap-server/target/asapLibrary/BasicLuaConf.lua
	fi
}

connectASAP2YARN()
{
	
	if [[ -z $YARN_HOME ]]
	then
		echo -e "No home folder of Hadoop YARN has been provided."\
			"${blue}Thus IReS ASAP server will work${reset}"\
			"but ${red}no workflows can be executed!${reset}"
	else
						
		cd $IRES_HOME
		# update YARN_HOME/etc/hadoop/yarn-site.xml with the properties of resources/conf/yarn-site-min.xml file
		# if the property yarn.resource.hostname exists in YARN_HOME/etc/hadoop/yarn-site.xml file then this
		# property should not be inserted again and it is assumed to be correctly set.
		# update with the appropriate host name
		rm_host_exists=`sed -n '/<name>yarn\.resourcemanager\.hostname<\/name>/=' $YARN_HOME/etc/hadoop/yarn-site.xml`
		if [[ -z $rm_host_exists ]]
		then
			# specify host name running of HADOOP YARN
			echo -e "\n\nNo hostname or ip address for ResourceManager was found."
			read -p "Give the full name or the ip of the host where ResourceManager runs: " HOST_NAME
			rm_host_exists="\t<!-- Configurations for Resource Manager -->\n\t<property>\n\t\t<name>yarn.resourcemanager.hostname</name>\n\t\t"
			rm_host_exists="$rm_host_exists<value>$HOST_NAME</value>\n\t\t<description>The hostname of the RM.</description>\n\t</property>\n\n"
		else
			rm_host_exists=""
		fi
		# update YARN_HOME/etc/hadoop/core-site.xml with the properties of resources/conf/core-site-min.xml file
		# if the property fs.defaultFS exists in YARN_HOME/etc/hadoop/core-site.xml file then this
		# property should not be inserted again and it is assumed to be correctly set.
		# update with the appropriate host name
		nm_host_exists=`sed -n '/<name>fs\.defaultFS<\/name>/=' $YARN_HOME/etc/hadoop/core-site.xml`
		if [[ -z $rm_host_exists ]]
		then
			# specify host name running of HADOOP YARN
			echo -e "\n\nNo hostname or ip address for NameNode was found."
			read -p "Give the full name or the ip of the host where NameNode runs: " HOST_NAME
			description="The name of the default file system. A URI whose scheme and authority\n \
				     determine the FileSystem implementation. The uri's scheme determines\n \
      				     the config property (fs.SCHEME.impl) naming the FileSystem implementation\n
      				     class. The uri's authority is used to determine the host, port, etc. for\n
			      	     a filesystem."
			nm_host_exists="\t<!-- Configurations for NameNode -->\n\t<property>\n\t\t<name>fs.defaultFS</name>\n\t\t"
			nm_host_exists="$nm_host_exists<value>hdfs://$HOST_NAME:9000</value>\n\t\t<description>$description</description>\n\t</property>\n\n"
		else
			nm_host_exists=""
		fi
		#extract all the properties that reside inside <configuration></configuration> tags
		start=`sed -n '/<configuration>/=' resources/conf/yarn-site-min.xml`
		start=$(( $start + 1))
		end=`sed -n '/<\/configuration>/=' resources/conf/yarn-site-min.xml`
		end=$(( $end - 1))
		properties=`sed -n "$start","$end"p resources/conf/yarn-site-min.xml`
		# add minimum set of properties
		sed -i 's_<\/configuration>__' $YARN_HOME/etc/hadoop/yarn-site.xml
		echo -e "$rm_host_exists$properties\n</configuration>" >> $YARN_HOME/etc/hadoop/yarn-site.xml
		#extract now just all the properties that reside inside <configuration></configuration> tags
		start=`sed -n '/<configuration>/=' resources/conf/core-site-min.xml`
		start=$(( $start + 1))
		end=`sed -n '/<\/configuration>/=' resources/conf/core-site-min.xml`
		end=$(( $end - 1))
		properties=`sed -n "$start","$end"p resources/conf/core-site-min.xml`
		# add minimum set of properties
		sed -i 's_<\/configuration>__' $YARN_HOME/etc/hadoop/core-site.xml
		echo -e "$nm_host_exists$properties\n</configuration>" >> $YARN_HOME/etc/hadoop/core-site.xml

		# copy Hadoop YARN configuration files into ASAP server
		cp $YARN_HOME/etc/hadoop/yarn-site.xml $IRES_HOME/asap-platform/asap-server/target/conf
		cp $YARN_HOME/etc/hadoop/core-site.xml $IRES_HOME/asap-platform/asap-server/target/conf

		# set asap.properties file
		# append
		# path of hdfs command of Hadoop YARN
		sed -i "s:^asap\.hdfs_path.*:asap\.hdfs_path = "$YARN_HOME"/bin/hdfs:" $IRES_HOM/Easap-platform/asap-server/src/main/resources/asap.properties
		# path of asap command of ASAP server which does the reporting( taking measures)
		sed -i "s:^asap\.asap_path.*:asap\.asap_path = /home/"$USER"/asap/bin/asap:" $IRES_HOME/asap-platform/asap-server/src/main/resources/asap.properties

		# add Hadoop YARN classpath to $IRES_HOME/asap-server/main/src/scripts/asap-server file
		YARN_CLASSPATH=`${YARN_HOME}/bin/hadoop classpath`
		sed -i "s@^YARN_CLASSPATH=.*@YARN_CLASSPATH=\"${YARN_CLASSPATH}\"@" $IRES_HOME/asap-platform/asap-server/src/main/scripts/asap-server
		# update asap-platform/asap-server/asapLibrary/BasicLuaConf.lua
		sed -i "s@^YARN_CLASSPATH.*=.*@YARN_CLASSPATH=\"${YARN_CLASSPATH}\"@" $IRES_HOME/asap-platform/asap-server/asapLibrary/BasicLuaConf.lua
		sed -i "s@^YARN_CLASSPATH.*=.*@YARN_CLASSPATH=\"${YARN_CLASSPATH}\"@" $IRES_HOME/asap-platform/asap-server/defaultAsapLibrary/BasicLuaConf.lua
		if [[ -e $IRES_HOME/asap-platform/asap-server/target/asapLibrary/BasicLuaConf.lua ]]
		then
			sed -i "s@^YARN_CLASSPATH.*=.*@YARN_CLASSPATH=\"${YARN_CLASSPATH}\"@" $IRES_HOME/asap-platform/asap-server/target/asapLibrary/BasicLuaConf.lua
		fi
		cd -
	fi
}

while getopts ":hgr:s:c:l:" opt;
	do
		case $opt in
		h)
			usage
			exit
			;;
			
		g)	
			INSTALL_MODE="gui"
			exit
			;;
			
		r)  
			command=($OPTARG)
			if [ "$command" = "start" -o  "$command" = "stop" -o "$command" = "restart" ]
			then
				echo -e "$command-ing ASAP server"
				$IRES_HOME/asap-platform/asap-server/src/main/scripts/asap-server $command			
			else
				echo -e "No valid command has been given. Command is $command.\n"
				usage
			fi
			exit
			;;
		s)	
			if [[ ! -z $OPTARG ]]
			then
				IRES_HOME=($OPTARG)
				if [[ $IRES_HOME == "." ]]
				then
					IRES_HOME=`pwd`	
				fi
			fi
			setIresHome
			exit
			;;

		c)	
			OLD_IFS=$IFS
			IFS=,
			declare -a tmp=($OPTARG)
			if [[ ! -z $tmp ]]
			then
				YARN_HOME=${tmp[0]}
				IRES_HOME=${tmp[1]}
			else
				usage
				exit
			fi
			IFS=$OLD_IFS
			connectASAP2YARN
			exit
			;;
		l)
			OLD_IFS=$IFS
			IFS=,
			logs=false
			declare -a tmp=($OPTARG)
			if [[ ! -z $tmp ]]
			then
				if [[ ${tmp[0]} =~ ^[0]{0,}[1-9]{1}[0-9]{0,}$ ]]
				then
					if [[ ! -z ${tmp[1]} && ${tmp[1]} == "0" ]]
					then
						tail -f -n ${tmp[0]} /tmp/asap-server.log
					else
						tail -n ${tmp[0]} /tmp/asap-server.log
					fi
					logs=true
				else
					if [[ ${tmp[0]} =~ ^[0]{1,}$ && -z ${tmp[1]} ]]  
					then
						tail -f /tmp/asap-server.log
						logs=true
					fi
				fi
			fi
			IFS=$OLD_IFS
			if [[ $logs == false ]]
			then
				echo -e "Wrong arguments given for -l flag.You given '$OPTARG'.\n"
				usage
			fi
			exit
			;;
		:)
			echo -e "\nOption -$OPTARG requires its argument.\n"
			usage
			exit
			;;			
		?)
			echo -e "\nWrong parameters where given.\nGiven -$OPTARG \n"
			usage
			exit
			;;
		esac
	done

configured=false
while ! $configured
do
	if [[ $INSTALL_MODE == "cli" ]]
	then
		echo -e $msg
		echo -e "WELCOME TO ASAP SERVER AND IReS-Platform INSTALLATION!\n"

		# build ASAP server
		rcd=`pwd`
		cd $IRES_HOME
		percentage=0
		module_index=0
		echo -e "ASAP server is under construction ... please wait"
		echo -e "Building ..."
		for module in ${modules[@]}
		do
			module_index=$(( $module_index + 1 ))
			echo "|--> ${blue}$module${reset} module( $module_index/${#modules[@]})"
			cd $module
			# build in quiet mode i.e. print only errors
			mvn clean install -DskipTests -P asapCluster-q > log.txt
			# check for errors
			errors=`cat log.txt | grep ERROR`
			#if [[ ! -z $errors ]]
			#then
			#	echo -e "$errors"
			#	echo -e "List of errors found!"
			#	echo -e "$errors\n\n${red}Contact administrator to fix them.${reset}"
			#	exit
			#fi
			# remove log.txt since everything went well
			rm log.txt
			cd ../
			percentage=$(( $percentage + 33))
			#echo $percentage 
		done
		cd $rcd
		# inform about builting success
		echo -e "ASAP server was built successfully.\n"

		echo -e "ASAP server environmental parameters setting"
		#update ASAP server configuration about IReS home folder
		setIresHome
		#connect ASAP to YARN
		echo -e "CONNECTING IReS WITH HADOOP YARN"
		read -p "Give the home folder( absolute path) of Hadoop YARN, if it exists, else press <Enter>: " YARN_HOME
		connectASAP2YARN
	
	else
		# specify home folder of HADOOP YARN
		YARN_HOME=`zenity	--title="Specify home folder of Hadoop YARN" \
					--file-selection \
					--directory `
		# specify host name running of HADOOP YARN
		HOST_NAME=`zenity	--entry \
					--entry-text="Full host name or host ip" \
					--title="Specify the full name or the ip of host running Hadoop YARN" \
					--text="Specify the full name or the ip of host running Hadoop YARN"`
		
		zenity	--info \
		 		--title="Welcome to ASAP server!" \
		 		--text="This is the first time configuration of server."

		setIresHome
		connectASAP2YARN	

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
	
	fi
	# echo $HOST_NAME
	# echo $YARN_HOME
	# echo $IRES_HOME
	# write configuration properties into asap_conf file
	conf=( $YARN_HOME $IRES_HOME)
	configured=true
done
