#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: none
Host System			: Ubuntu >= 10.04 LTS
Hadoop				: 1.x
INFO

<<DESCRIPTION
The function that executes the 1st experiment that examines Multiway and TwoByTwo
performance against relation size
DESCRIPTION

#important: all undefined variables in here are assumed that are defined inside
#the environment that calls this script

experiment1()
{
	strings=(MULTIWAY TWOBYTWO)
	jar=(Multi.jar TBTwo.jar)
	class=(Multiway TwoByTwo)
	echo -e "\n"${strings[$1]}
	echo 
	#for all type of joins
	for jointype in ${join_structures[@]}
	do
		print_parameter "$jointype"

		for blcksize in ${blocksize[@]}
		do
			mb=$(($blcksize/1024**2))
			print_parameter  "$mb MB"
	
			for records in ${records_sizes[@]}
			do
				print_parameter "records" $records
				genericoptions=( "-D dfs.block.size=$blocksize")
				#the source of input files
				input=$records
				args=( $jointype/$jointype.txt  $records $mb $input)
			
				$HOME_HADOOP_EXECUTION jar $HOME/${jar[$1]} ${class[$1]} ${genericoptions[@]} ${args[@]}
			done
		done
	done
}

experiment1 "$1"
