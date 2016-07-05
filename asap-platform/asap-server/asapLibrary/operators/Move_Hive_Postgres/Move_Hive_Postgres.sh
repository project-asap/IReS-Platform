#!/bin/bash
export HADOOP_HOME='/home/bill/PhD/projects/yarn'
HIVE_HOME='/home/bill/PhD/projects/hive'

echo "exporting table from HIVE"
mkdir t1
$HADOOP_HOME/bin/hadoop fs -copyToLocal $HIVE_HOME/warehouse/$2/* t1
rm /tmp/out
for x in $(ls t1/*);
do
        #echo $x
        cat $x >> /tmp/out
done
chown -R postgres:postgres /tmp/out
ls -ltr 
rm -r t1

echo "loading table to POSTGRES"
sudo -u postgres psql $1 -c "DROP TABLE $2;"
sudo -u postgres psql $1 -c "CREATE TABLE $2 $3;"
sudo -u postgres psql $1 -c "COPY $2 FROM '/tmp/out' WITH DELIMITER AS '|';"
rm /tmp/out
