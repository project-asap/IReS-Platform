#!/bin/bash

# script used to automate the deployment procedure

run_scenario(){
DESCRIPTION=$1
SHORT_DES=$(basename $1)

python -m deployer -d $DESCRIPTION -s state-files/${SHORT_DES}-state -a launch && mail-alert "$(DESCRIPTION) deployed"

# waiting for launch

HOSTNAME=$(python -m deployer -l state-files/${SHORT_DES}-state -a show | grep hostname | head -n 1 | tr "\'" "\t" | awk '{print $5}' )
HOST=$(ssh $HOSTNAME "hostname")

if [ "$HOST" == "master1" ]; then
	ssh $HOSTNAME < examples/hadoop/master/run_benchmarks.sh
	scp $HOSTNAME:/tmp/times.csv results/${SHORT_DES}-state 
	python -m deployer -l state-files/${SHORT_DES}-state -a terminate && rm state-files/${SHORT_DES}-state && mail-alert "$(DESCRIPTION) terminated"
fi
}

run_scenario "examples/hadoop/deployments/case-9n-1c.json" > /tmp/ggian_logs.txt &
run_scenario "examples/hadoop/deployments/case-9n-4c.json" > /tmp/celar_logs.txt &
