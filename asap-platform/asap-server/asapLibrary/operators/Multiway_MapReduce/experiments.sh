#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: 03/ 10/ 2013, 23 / 02 / 2013
Host System			: Ubuntu 13.04, 12.10, 12.04LTS, 10.04 LTS
Hadoop				: 1.0.x - 1.1.2
INFO

<<DESCRIPTION
This script runs all the experiments of my thesis
DESCRIPTION

usage()
{
	cat << EOF
	usage: $0 OPTIONS

	This script computes either a multiway join algorithm or a sequence of two 
	way join's algorithm or both of them. The join may executed on the structure
	of chainjoin, starjoin, or a simplejoin ( neither a chain join nor a starjoin).
	The joins( algorithms) are executed sequentially.
	
	The joins may need a preprocessing step.

	OPTIONS:
	
	-h
		show this message

	-p j,i
		to execute preprocessing step on joins i. i has the form

		join_struct1,join_struct2,join_struct3, ...
		
		j must be one of b, m, t.
		  j = m, preprocessing step for multiway join 
		  j = t, preprocessing step for twobytwo join
		  j = b, preprocessing step for both multiway and twobytwo

	-e j,i
		specify which algorithm to apply on join structures. The available algo-
		rithms are multiway and sequence of two way joins. The available struc-
		tures are chain join, star join and simple join. An algorithm is applied
		on every specified structure.
		
		j must be one of b, m, t.
		  j = m, execute multiway join 
		  j = t, execute twobytwo join
		  j = b, execute both multiway and twobytwo
		
		i has the form,
		
		join_struct1,join_struct2,join_struct3,...
		
	-x j
		choose which experiment you want to run. The available experiments are,
		  j = 1 for different block sizes and amount of records execute any join
		  		algorithm
		  j = 2 for different amount of reducers and amount of slaves execute any
		  		join algorithm
		  j = 3 execute all experiments
			
	-a
		execute both multiway and sequence of two way joins without executing any
		preprocessing step

	-i path
		the relative path from where the input files i.e. the join relations will
		be read 

EOF
}

#by default
#do not execute any preprocessing step
join_preprocessing=
#and for these join structures
join_structures=
#execute both multiway and two by two join's algorithms
multi_or_two=
#for block size = 64MB, 128MB, 256MB, 512MB, 1024MB
blocksize=( 67108864) #134217728 268435456 536870912 1073741824)
#for this amount of records / relation
records_sizes=( 4) #10000 100000 1000000) #10000000 100000000 200000000)
#for this amount or reducers
reducers_amount=( 4) #6 8 10 16 20 40 60 80 100)
#however you can preprocess some join structures
preproc_join_structs=
#if you say so
preproc_multi_or_two=
#which experiment do you want to execute?
experiment=

while getopts ":hp:e:x:ai:" opt;
	do
		case $opt in
		h)
			usage
			exit
			;;
		p)
			join_preprocessing=true
			IFS=,
			declare -a valid_options=(b m t)
			declare -a tmp=($OPTARG)
			preproc_multi_or_two=${tmp[0]}
			preproc_join_structs=("${tmp[@]:1:$((${#tmp[@]} - 1))}")
#			echo ${preproc_join_structs[@]}
			if [[ ! "${valid_options[@]}" =~ "${preproc_multi_or_two}" ]]
			then
				echo -e "\nOption -$opt was given wrong first argument \"${tmp[0]}\". See usage below.\n"
				usage
				exit
			fi
			;;
		e)
			IFS=,
			declare -a valid_options=(b m t)
			declare -a tmp=($OPTARG)
			multi_or_two=${tmp[0]}
			declare -a join_structures=("${tmp[@]:1:$((${#tmp[@]} - 1))}")
#			echo ${join_structures[@]}
			if [[ ! "${valid_options[@]}" =~ "${multi_or_two}" ]]
			then
				echo -e "\nOption -$opt was given wrong first argument \"${tmp[0]}\". See usage below.\n"
				usage
				exit
			fi
			;;
		x)
			declare -a valid_options=(1 2 3)
			experiment=$OPTARG
			if [[ ! "${valid_options[@]}" =~ "$experiment" ]]
			then
				echo -e "\nOption -$opt was given wrong first argument \"$OPTARG\". See usage below.\n"
				usage
				exit
			fi
			;;
		a)
			join_structures=(chainjoin simplejoin starjoin)
			multi_or_two=b
			;;
		i)	declare -r input=($OPTARG)
#			echo -e "The relation files will be read from directory "$input
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
	
if [[ -z $@ ]]
then
	echo -e "\nOption arguments are missing!\n"
	usage
	exit
fi

if [ "$experiment" == "" -a "$multi_or_two" != "" ]
then
	echo -e "\nYou must specify which experiment you want to run!\n"
	usage
	exit
fi

#import path, text format and slave amount definitions
##the period '.' is used instead of 'source' because it is POSIX compliant
#. resources/experiments/definitions.sh
. definitions.sh

#print a parameter output message
print_parameter()
{
	#takes records / relation as parameter
	echo
	parameter="$1 $2"
	echo $parameter
	#print "#" as many as the length of string "parameter_name $parameter_value"
	sharps=${#parameter}
	sharpline="#"
	for (( i=1; i < $sharps; i++ ))
	do
		sharpline="$sharpline#"
	done
	echo $sharpline
	echo
}

###################################
########## SET LOG FILES ##########
###################################
if [ ! -e "$HOME_OUT" ]
	then
		mkdir -p "$HOME_OUT"
fi

if [ ! -e "$FILE_JOBIDS_TXT" -o ! -f "$FILE_JOBIDS_TXT" ]
	then
		touch "$FILE_JOBIDS_TXT"
	else
		extension=`date +%F%a%T`
		mv "$FILE_JOBIDS_TXT" "$FILE_JOBIDS_TXT$extension"
		touch "$FILE_JOBIDS_TXT"
fi

if [ ! -e "$FILE_FAILED_JOBS_TXT" -o ! -f "$FILE_FAILED_JOBS_TXT" ]
	then
		touch "$FILE_FAILED_JOBS_TXT"
	else
		extension=`date +%F%a%T`
		mv "$FILE_FAILED_JOBS_TXT" "$FILE_FAILED_JOBS_TXT$extension"
		touch "$FILE_FAILED_JOBS_TXT"
fi

if [ ! -e "$FILE_TIMES_TXT" -o ! -f "$FILE_TIMES_TXT" ]
	then
		touch "$FILE_TIMES_TXT"
	else
		extension=`date +%F%a%T`
		mv "$FILE_TIMES_TXT" "$FILE_TIMES_TXT$extension"
		touch "$FILE_TIMES_TXT"
fi

if [ ! -e "$FILE_RECORDSIO_TXT" -o ! -f "$FILE_RECORDSIO_TXT" ]
	then
		touch "$FILE_RECORDSIO_TXT"
	else
		extension=`date +%F%a%T`
		mv "$FILE_RECORDSIO_TXT" "$FILE_RECORDSIO_TXT$extension"
		touch "$FILE_RECORDSIO_TXT"
fi

#################
#  EXPERIMENTS  #
#################

# PREPROCESSING #

#preprocess without executing any experiment
if [ "$join_preprocessing" == true -a "$experiment" == "" ]
then
	start=`date +%s`
	#include preprocessing.sh
	. preprocessing.sh
	#then call it for each jointype
	for jointype in ${preproc_join_structs[@]}
	do
		echo -e "Going to execute preprocessing.sh for $jointype"
#		. resources/experiments/preprocessing.sh $jointype $preproc_multi_or_two
		preprocessing $jointype $preproc_multi_or_two
		echo -e "Executed preprocessing.sh for $jointype"
	done
	preproc=`date +%s`
	preproc=$(( $preproc - $start))
fi

# EXPERIMENT 1 #

if [ "$experiment" == "3" -o "$experiment" == "1" ]
	then
		preproc=0
		multi1=0
		tbt1=0
		#Preprocessing is optional if it is done at least once for an amount of
		#reducers which agrees with experiment's amount of reducers.
		if $join_preprocessing
		then
			start=`date +%s`
			#include preprocessing.sh script
			. preprocessing.sh
			#then call it for each jointype
			for jointype in ${preproc_join_structs[@]}
			do
#				. resources/experiments/preprocessing.sh $jointype $preproc_multi_or_two
				preprocessing $jointype $preproc_multi_or_two
			done
			preproc=`date +%s`
			preproc=$(( $preproc - $start))
		fi

		case "$multi_or_two" in
		 b)
			start=`date +%s`
			. resources/experiments/experiment1.sh 0
			multi1=`date +%s`
			multi1=$(( $multi1 - $start))

			start=`date +%s`
			. resources/experiments/experiment1.sh 1
			tbt1=`date +%s`
			tbt1=$(( $tbt1 - $start))
			;;
		 m)
			start=`date +%s`
#		 	. resources/experiments/experiment1.sh 0
		 	. experiment1.sh 0
			multi1=`date +%s`
			multi1=$(( $multi1 - $start))
		 	;;
		 t)
			start=`date +%s`
		 	. resources/experiments/experiment1.sh 1
			tbt1=`date +%s`
			tbt1=$(( $tbt1 - $start))
		 	;;
		esac

		echo -e "\nExperiment 1"
		echo -e "============"
		printf "%20s | %s | %10s | %10s |\n" " " "Preprocessing" "Multiway" "TwoByTwo"
		printf "%20s %15s | %10s | %10s |\n" "Execution time (sec)" "$preproc" "$multi1" "$tbt1"
		printf "%20s\n" "--------------------"
		printf "Total time (sec): \t $(( $preproc + $multi1 + $tbt1))\n\n"
fi

# EXPERIMENT 2 #

if [ "$experiment" == "3" -o "$experiment" == "2" ]
	then
		multi2=0
		tbt2=0
		case "$multi_or_two" in
		 b)
			start=`date +%s`
			. resources/experiments/experiment2.sh 0
			multi2=`date +%s`
			multi2=$(( $multi2 - $start))

			start=`date +%s`
			. resources/experiments/experiment2.sh 1
			tbt2=`date +%s`
			tbt2=$(( $tbt2 - $start))
			;;
		 m)
			start=`date +%s`
#		 	. resources/experiments/experiment2.sh 0
		 	. experiment2.sh 0
			multi2=`date +%s`
			multi2=$(( $multi2 - $start))
		 	;;
		 t)
			start=`date +%s`
		 	. resources/experiments/experiment2.sh 1
			tbt2=`date +%s`
			tbt2=$(( $tbt2 - $start))
		 	;;
		esac

		echo -e "\nExperiment 2"
		echo -e "============"
		printf "%20s | %10s | %10s |\n" " " "Multiway" "TwoByTwo"
		printf "%20s %10s | %10s |\n" "Execution time (sec)" "$multi1" "$tbt1"
		printf "%20s\n" "--------------------"
		printf "Total time (sec): \t $(( $multi2 + $tbt2))\n\n"
fi

exit
