#!/bin/bash

echo -e "InnerSQL_Postgres\n"

SQL_QUERY=$1
DATABASE=$2

echo -e "SQL query is " $SQL_QUERY
echo -e "Database is " $DATABASE


#sudo -u postgres psql -d $DATABASE -c $SQL_QUERY
sudo -u postgres psql -d $DATABASE -c "DROP TABLE IF EXISTS part_agg; CREATE TABLE part_agg AS SELECT l_partkey AS agg_partkey, 0.2 * AVG(l_quantity) AS avg_quantity, l_extendedprice AS agg_extendedprice FROM lineitem GROUP BY l_partkey, l_extendedprice"

