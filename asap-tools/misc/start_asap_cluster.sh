#!/usr/bin/env bash

master_ip=$(nova list | grep "SHUTOFF" | grep master | awk '{ print $12}' | sed "s/.*=//g" | sed "s/,//g")
slave_ips=$(nova list | grep "SHUTOFF" | grep slave | awk '{ print $12}' | sed "s/.*=//g" | sed "s/,//g")


user=root
master=$(nova list | grep "SHUTOFF" | grep master | awk '{ print $12}' | sed "s/.*=//g" | sed "s/,//g")

H_HOME=/opt/hadoop-2.7.0
S_HOME=/opt/spark-1.5.1-bin-hadoop2.6

# !!!USE ';' and !!!!
master_script="
$H_HOME/sbin/start-dfs.sh ;
$H_HOME/sbin/start-yarn.sh ;
$S_HOME/sbin/start-all.sh ;
"
#echo $master_script


slave_names=$(nova list | grep "SHUTOFF" | grep "slave"  | awk '{ print $4}')
master_name=$(nova list | grep "SHUTOFF" | grep "master"  | awk '{ print $4}')

echo "===== STARTING THE CLUSTER  ======"

echo == starting the master \($master_name\)

nova start $master_name

for name in $slave_names; do
	echo == starting: $name
	nova start $name
done

# wait for slaves to come online
for ip in $slave_ips; do
	echo == Waiting for $ip
	a='-'
	while [[ $a !=  'hello' ]]; do
	         a=$(ssh $ip "echo hello" 2>/dev/null)
	done
done

a='-'
while [[ $a !=  'hello' ]]; do
                 a=$(ssh $master_ip "echo hello" 2>/dev/null)
done


ssh $user@$master -C $master_script
