#!/usr/bin/env bash

user=root
master=$(nova list | grep "ACTIVE" | grep master | awk '{ print $12}' | sed "s/.*=//g" | sed "s/,//g")

H_HOME=/opt/hadoop-2.7.0
S_HOME=/opt/spark-1.5.1-bin-hadoop2.6

# !!!USE ';' and !!!!
master_script="
$S_HOME/sbin/stop-all.sh ;
$H_HOME/sbin/stop-yarn.sh ;
$H_HOME/sbin/stop-dfs.sh ;
"
echo "===== STOPPING SPARK AND HADOOP SERVICES ======"
#ssh $user@$master -C $master_script
#echo $master_script


slave_names=$(nova list | grep "ACTIVE" | grep "slave"  | awk '{ print $4}')
master_name=$(nova list | grep "ACTIVE" | grep "master"  | awk '{ print $4}')

echo "===== SHUTTING DOWN SLAVES ======"
for name in $slave_names; do
	echo == stopping:  $name
	nova stop $name
done

echo == stopping the master \($master_name\)
nova stop  $master_name

