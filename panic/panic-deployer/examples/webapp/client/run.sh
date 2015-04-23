#!/bin/bash

# populate the database with data
# each populate command fills the table with 100k rows -> 100MB of data
ab -n 10 -c 1 "http://apache1/queries.php?action=populate" > /root/populate_output.csv


for OFFSET in $(seq 100 100 500); do
for CONCURRENT_QUERIES in $(seq 50 50 400); do
  ab -n 800 -c $CONCURRENT_QUERIES "http://apache1/queries.php?action=get&offset=$OFFSET" > /root/offset_${OFFSET}_cq_${CONCURRENT_QUERIES}.csv
done
done