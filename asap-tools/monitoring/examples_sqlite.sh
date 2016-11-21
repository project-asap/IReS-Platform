# Be sure to check the requirements

# remove previous db file
rm my_metrics.db &>/dev/null

create=" CREATE TABLE experiment1 (id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER, m1 INTEGER, m2 INTEGER, metrics_timeline STRING, date DATE DEFAULT (datetime('now','localtime')));"

# create the database file
echo $create | sqlite3 my_metrics.db

# set the database backend
./reporter_cli.py set-backend -b backend=SqliteBackend file=my_metrics.db

# Report, for experiment 'my_experiment', some metrics and their values
./reporter_cli.py report -e experiment1 -m m1=1 m2=2
./reporter_cli.py report -e experiment1 -m m1=2 m2=3
./reporter_cli.py report -e experiment1 -m m1=3 m2=4
./reporter_cli.py report -e experiment1 -m m1=5 m2=20

# Query the data you have reported to the back-end
echo "Querying for m2<4" 
./reporter_cli.py query -q  "select * from experiment1 where m2<4"

# Query and get the data as dict
echo "Querying for m2<4:"
./reporter_cli.py query -q  "select * from experiment1 where m2<4" -dict

# Plot the data you stored
echo "Plotting \n"
./reporter_cli.py plot-query -q  "select m1, m2 from experiment1;" -pp xlabel=bull-x title='my title' ylabel=foos

# start the monitoring process
echo Starting Monitoring
./monitor.py -eh  192.168.5.242 &
echo Sleeping
sleep 3

# report data including monitoring metrics
echo Reporting with monitoring data
./reporter_cli.py report -e experiment1 -m m1=0 m2=0 -cm

#query for those monitoring metrics
echo "Query for metrics:"
./reporter_cli.py query -q  "select metrics_timeline from experiment1 where m1=0"

