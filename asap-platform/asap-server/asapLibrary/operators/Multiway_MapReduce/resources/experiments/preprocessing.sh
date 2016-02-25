#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: none
Host System			: Ubuntu >= 10.04 LTS
Hadoop				: 1.x
INFO

<<DESCRIPTION
The function that executes the neccessary preprocessing step before running any
experiment
DESCRIPTION

#important: all undefined variables in here are assumed that are defined inside
#the environment that calls this script

preprocessing()
{
	#execute the algorithm described in "Optimizing joins in a Map - Reduce en-
	#vironment" paper by Foto N. Afrati and Jeffrey D. Ullman
	jointype="$1"
	multi_or_two_processing="$2"
	echo -e "####################################"
	echo -e "$jointype PREPROCESSING STEP STARTED"
	echo -e "####################################"
	echo -e "\n"${bold}$jointype${reset}" preprocessing\n"
	#delete jointype directory in HDFS to refresh the results of preprocessing
	$HOME_YARN_EXECUTION fs -rm -r /user/hadoop/optimizingJoins/$jointype
	echo
	#create the appropriate directories to all nodes, if they don't exist in master,
	#where the input and output files of the preprocessing and the join algorithms
	#will be found. Also copy the jointype.txt description file.
	join_home=$HOME/$jointype
	if [ ! -e "$join_home" ]
		then
			pdsh -R ssh -w master,slave[${SLAVES[0]}-${SLAVES[ $(( ${#SLAVES[@]} - 1))]}] "if [[ ! -e $join_home ]]; then mkdir -p $joinhome; fi"
			cp resources/joins/$jointype.txt $join_home
			pdcp -R ssh -w master,slave[${SLAVES[0]}-${SLAVES[ $(( ${#SLAVES[@]} - 1))]}] $HOME/resources/joins/4Relations/$jointype.txt /home/hadoop/$jointype
	fi
	
	#run the preprocessing step for this jointype
	$YARN_JAR_EXECUTION jar $HOME/PreProc.jar PreProcessing $jointype/$jointype.txt $multi_or_two_processing
	
	#tar preprocessing result files and send them to all slave nodes in case they
	#need them during the actual join algorithm
	jointar=$HOME/$jointype.tar
	cd $join_home
	tar -cpf $jointar gk*.txt mapkey.txt mapper.txt newk.txt
	chmod +x $jointar
	cd ../
	
	pdcp -R ssh -w master,slave[${SLAVES[0]}-${SLAVES[${#SLAVES[@]} - 1]}] $jointar $join_home
	pdsh -R ssh -w master,slave[${SLAVES[0]}-${SLAVES[${#SLAVES[@]} - 1]}] "tar -xf $join_home/$jointype.tar -C $join_home"

	preproc=`date +%s`

	echo -e "#####################################"
	echo -e "FINISHED $jointype PREPROCESSING STEP"
	echo -e "#####################################\n"
}
