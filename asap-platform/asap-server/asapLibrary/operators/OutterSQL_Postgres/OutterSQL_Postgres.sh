#!/bin/bash

echo -e "OutterSQL_Postgres\n"

SQL_QUERY=$1
DATABASE=$2

#sudo -u postgres psql -c $DATABASE -c $SQL_QUERY
sudo -u postgres psql -c $DATABASE -c "DROP TABLE IF EXISTS FINAL_RESULTS; CREATE TABLE FINAL_RESULTS AS SELECT SUM(agg_extendedprice) / 7.0 AS avg_yearly  FROM part, part_agg  WHERE p_partkey = agg_partkey AND p_brand = 'Brand#33' AND p_container = 'MED BAG' LIMIT 1"
