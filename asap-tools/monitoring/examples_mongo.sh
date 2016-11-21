# Be sure to check the requirements

# drop any previous data
mongo metrics --eval "db.experiment1.drop()" &>/dev/null

# set the database backend
./reporter_cli.py set-backend -b backend=mongo port=27017 host=localhost

# Report, for experiment 'my_experiment', some metrics and their values
./reporter_cli.py report -e experiment1 -m m1=1 m2=2
./reporter_cli.py report -e experiment1 -m m1=2 m2=3
./reporter_cli.py report -e experiment1 -m m1=3 m2=4
./reporter_cli.py report -e experiment1 -m m1=5 m2=20


# Query the data you have reported to the back-end
# for mongo be sure to use ' quotes for operators containing $
# also always use quotes(") for attribute names (here "m1")
echo === "Querying for m2<4" 
./reporter_cli.py query -e experiment1 -q  '{"m1":{"$gt":1}}, {"_id":0}'

# Query and get the data as dict
echo === "Querying for m2<4:"
./reporter_cli.py query -e experiment1 -q  '{"m1":{"$gt":1}}' -dict

# Plot the data you stored
echo === "Plotting \n"
./reporter_cli.py plot-query -e experiment1 -q  '{}, {"m1":1, "m2":1,"_id":0}' -pp xlabel=bull-x title='my title' ylabel=foos 
# start the monitoring process
echo === Starting Monitoring
./monitor.py -eh  192.168.5.242 &
echo === Sleeping
sleep 3

# report data including monitoring metrics
echo === Reporting with monitoring data
./reporter_cli.py report -e experiment1 -m m1=0 m2=0 -cm

#query for those monitoring metrics
echo === "Query for metrics:"
./reporter_cli.py query -e experiment1 -q  '{"m1":0}, {"_id":0}' -dic

