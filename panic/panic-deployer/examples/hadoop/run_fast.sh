#!/bin/bash

# script used to automate the deployment procedure

run_scenario(){
DESCRIPTION=$1
SHORT_DES=$(basename $1)

python -m deployer -d $DESCRIPTION -s state-files/${SHORT_DES}-state -a launch > /tmp/${SHORT_DES}-deployment-logs && mail-alert "$DESCRIPTION deployed and state saved in state-files/${SHORT_DES}-state" 

# waiting for launch

MASTER_HOSTNAME=$(python -m deployer -l state-files/${SHORT_DES}-state -a show | grep hostname | head -n 1 | tr "\'" "\t" | awk '{print $5}' )
HOST=$(ssh $MASTER_HOSTNAME "hostname")

if [ "$HOST" == "master1" ]; then
scp examples/hadoop/master/run_benchmarks.sh ${MASTER_HOSTNAME}:/root/script.sh
ssh $MASTER_HOSTNAME "chmod +x script.sh; /root/script.sh"
scp $MASTER_HOSTNAME:/tmp/times.csv results/${SHORT_DES}-state 
python -m deployer -l state-files/${SHORT_DES}-state -a terminate && rm state-files/${SHORT_DES}-state && mail-alert "$DESCRIPTION terminated"
fi
}


celar_deployments(){
#run_scenario "examples/hadoop/deployments/case-3n-1c.json"
#run_scenario "examples/hadoop/deployments/case-10n-1c.json"
#run_scenario "examples/hadoop/deployments/case-10n-2c.json"
#run_scenario "examples/hadoop/deployments/case-10n-4c.json"
#run_scenario "examples/hadoop/deployments/case-3n-4c.json"
#run_scenario "examples/hadoop/deployments/case-5n-1c.json"
#run_scenario "examples/hadoop/deployments/case-5n-2c.json"
#run_scenario "examples/hadoop/deployments/case-5n-4c.json"
#run_scenario "examples/hadoop/deployments/case-6n-1c.json"
#run_scenario "examples/hadoop/deployments/case-6n-2c.json"
#run_scenario "examples/hadoop/deployments/case-6n-4c.json"
#run_scenario "examples/hadoop/deployments/case-7n-1c.json"
#run_scenario "examples/hadoop/deployments/case-8n-2c.json"
#run_scenario "examples/hadoop/deployments/case-8n-1c.json"
#run_scenario "examples/hadoop/deployments/case-8n-4c.json"
#run_scenario "examples/hadoop/deployments/case-9n-4c.json"
echo "nothing to do"
}

ggian_deployments(){
#run_scenario "examples/hadoop/deployments/case-2n-1c.json"
#run_scenario "examples/hadoop/deployments/case-2n-2c.json"
#run_scenario "examples/hadoop/deployments/case-2n-4c.json"
#run_scenario "examples/hadoop/deployments/case-3n-2c.json"
#run_scenario "examples/hadoop/deployments/case-4n-1c.json"
#run_scenario "examples/hadoop/deployments/case-4n-2c.json"
#run_scenario "examples/hadoop/deployments/case-4n-4c.json"
#run_scenario "examples/hadoop/deployments/case-7n-2c.json"
#run_scenario "examples/hadoop/deployments/case-7n-4c.json"
#run_scenario "examples/hadoop/deployments/case-9n-1c.json"
run_scenario "examples/hadoop/deployments/case-9n-2c.json"
}


celar_deployments & ggian_deployments
