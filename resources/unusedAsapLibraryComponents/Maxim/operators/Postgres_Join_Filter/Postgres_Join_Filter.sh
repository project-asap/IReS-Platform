#!/bin/bash
sudo -u postgres psql $4 -c "DROP TABLE $3; "
sudo -u postgres psql $4 -c "CREATE TABLE $3 AS SELECT NATIONKEY, TOTALPRICE FROM $1 LEFT JOIN $2 ON $1.CUSTKEY=$2.CUSTKEY ;" 