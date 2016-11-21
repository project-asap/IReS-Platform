if [ "$#" -ne 1 ]; then
    echo "Please specify a directory to load from"
    exit
fi

#create DB
echo "DROP DATABASE IF EXISTS tpch;" | psql -U postgres
echo "CREATE DATABASE tpch OWNER asap;" | psql -U postgres
psql -U asap tpch -f tpch-create.sql 


echo loading
# cd to dir
cd $1
#load from files
echo "\copy part from 'part_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy lineitem from 'lineitem_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy customer from 'customer_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy supplier from 'supplier_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy orders from 'orders_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy nation from 'nation_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy partsupp from 'partsupp_strip.tbl' with delimiter as '|';" | psql -U asap tpch
echo "\copy region from 'region_strip.tbl' with delimiter as '|';" | psql -U asap tpch
