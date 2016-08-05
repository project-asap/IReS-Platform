#!/bin/bash
echo "exporting table from HIVE"
mkdir t1
/opt/hadoop-2.6.0/bin/hadoop fs -copyToLocal /opt/warehouse/$2/* t1
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
