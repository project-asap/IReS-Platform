#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: none
Host System			: Ubuntu >= 10.04 LTS
Hadoop				: 2.x
INFO

<<DESCRIPTION
The function that executes the 2nd experiment that examines Multiway and TwoByTwo
performance against reducers and available cpus
DESCRIPTION

#important: all undefined variables in here are assumed that are defined inside
#the environment that calls this script

#in this experiment 
##the relation size is fixed to relation_size, default 1M
relation_size=1000000
##and the amount of cpus varies by excluding slave nodes. At the beginning of this
##experiment the cluster has its full capacity of nodes and thereafter step slaves
##get shut down per experiment round. The array of slaves,"slaves" on which this
##experiment runs can be changed by changing the value of variable nssa ( not
##sleeping slaves amount) that cannot be < 0 and > the length of "slaves"
step=1
if [[ "$step" -eq 0 ]]
then
	echo -e "Variable 'step' cannot be 0 (zero) because for this value the expe-"
	echo -e "riment will be run multiple times without any change in cluster's "
	echo -e "configuration. This is meaningless."
	exit
fi
#due to the fact that array indexing starts from 0, nssa should be +1 than the
#desired amount of slaves to be always operating e.g. for 4 slaves should be 5
nssa=2
slaves=("${SLAVES[@]:0:$(( ${#SLAVES[@]} - $nssa))}")
echo -e "Will be shutdown slaves " ${slaves[@]}"."

experiment2()
{
	strings=(MULTIWAY TWOBYTWO)
	jar=(Multi.jar TBTwo.jar)
	class=(Multiway TwoByTwo)
	preprocess=(m t)
	echo
	echo ${strings[$1]}
	echo
	#create folder optimizingJoins for storing the amount of reducers in file k.txt
	pdsh -R ssh -w slave[${SLAVES[0]}-${SLAVES[ $(( ${#SLAVES[@]} - 1))]}] "if [[ ! -e /home/hadoop/optimizingJoins ]]; then mkdir /home/hadoop/optimizingJoins; fi"
	#for all type of joins
	for jointype in ${join_structures[@]}
	do
		print_parameter "$jointype" ""

		for blcksize in ${blocksize[@]}
		do
			mb=$(($blcksize/1024**2))
			print_parameter $mb "MB"
	
			for reducers in ${reducers_amount[@]}
			do
				print_parameter "reducers" $reducers
				
				#update /home/hadoop/optimizingJoins/k.txt file which holds the amount
				#of reducers
				echo "$reducers" > $HOME/optimizingJoins/k.txt
				#inform for the new amount of reducers all cluster slave nodes
				echo -e "Update slaves with the new value of reducers"
				pdcp -R ssh -w slave[1-10] optimizingJoins/k.txt /home/hadoop/optimizingJoins/
				#execute the preprocessing step for the current join algorithm
				. resources/experiments/preprocessing.sh $jointype ${preprocess[$1]}
				working=$(( ${#slaves[@]} + $nssa + 1 ))
				shutdown=0
				echo -e "\nStart experiment with ${bold}$working nodes${reset} ( master included)\n"
				for slave in "${slaves[@]}"
				do
					if [[ $shutdown -ge "${#slaves[@]}" ]]
					then
						#only master and always operating slave nodes are running;
						#all the nodes that were supposed to sleep are already
						#sleeping although the value of "slave" may not reached the
						#last value of ${slaves[@]}
						break
					else
						#it is assumed that in the beginning all slaves are working
						genericoptions=("-D dfs.block.size=$blocksize")
						#retrieve the exact value of reducers
						reducers=`head -1 optimizingJoins/k.txt`
						reducers=$(( reducers))
						input=$relation_size
						args=($jointype/$jointype.txt $relation_size $mb $input $reducers $working)
		
						$YARN_JAR_EXECUTION jar $HOME/${jar[$1]} ${class[$1]} ${genericoptions[@]} ${args[@]}
				
						#after work is finished
						#stop job history server
						$HOME_YARN/sbin/mr-jobhistory-daemon.sh stop historyserver
						#stop yarn
						$HOME_YARN/sbin/stop-yarn.sh
						#stop dfs
						$HOME_YARN/sbin/stop-dfs.sh  

						#and step slaves go to sleep;
						echo
						for (( stp = $slave; stp < $slave + $step; stp++ ))
						do
							if [[ "$stp" -le ${#slaves[@]} ]]
							then
								echo -e "Shut down ${bold}slave$stp${reset}"
								echo slave$stp >> $HOME/excludeNodes.txt
								working=$(( $working - 1 ))
								((shutdown++))
							else
								break
							fi
						done
						if [[ $working -eq $(( $nssa + 1)) ]]
						then 
							echo -e "Only ${bold}master${reset} and always running slaves are running.\n"
							break
						else
							echo -e "There are still ${bold}$working working nodes${reset} ( master inlcluded).\n"
							#restart cluster
							#start dfs
							$HOME_YARN/sbin/start-dfs.sh  
							#start yarn
							$HOME_YARN/sbin/start-yarn.sh
							#start job history server
							$HOME_YARN/sbin/mr-jobhistory-daemon.sh start historyserver
							echo
							#wait until Hadoop gets out of safe mode
							echo -e "Wait for Hadoop cluster to come out from ${bold} SAFE MODE ${reset}\n"
							while true
							do
								safe=`$HOME_HADOOP_EXECUTION dfsadmin -safemode get | grep OFF | awk '{print $NF}'`
								if [ "$safe" == "OFF" ]
								then
									break
								fi
							done
						fi
					fi
				done
				#reset cluster to its defaults i.e. all nodes running
				#is hadoop running?
				running=`jps | grep JobTracker`
				if [[ "$running" ]]
				then
					#if Hadoop is running, stop it first in order to reset it
					#stop job history server
					$HOME_YARN/sbin/mr-jobhistory-daemon.sh stop historyserver
					#stop yarn
					$HOME_YARN/sbin/stop-yarn.sh
					#stop dfs
					$HOME_YARN/sbin/stop-dfs.sh  
					echo
				fi
				#wake up slaves fallen asleep
				cat /dev/null > $HOME/excludeNodes.txt
				echo -e "Restart cluster with all of its nodes running.\n"
				#start dfs
				$HOME_YARN/sbin/start-dfs.sh  
				#start yarn
				$HOME_YARN/sbin/start-yarn.sh
				#start job history server
				$HOME_YARN/sbin/mr-jobhistory-daemon.sh start historyserver
				echo
				#wait until Hadoop gets out of safe mode
				echo -e "Wait for Hadoop cluster to come out from ${bold} SAFE MODE ${reset}\n"
				while true
				do
					safe=`$HOME_YARN/bin/hdfs dfsadmin -safemode get | grep OFF | awk '{print $NF}'`
					if [ "$safe" == "OFF" ]
					then
						break
					fi
				done
				#restore /home/hadoop/optimizingJoins/k.txt file to its default
				#value which is 20
				echo "20" > $HOME/optimizingJoins/k.txt
			done
		done
	done
}

experiment2 "$1"
