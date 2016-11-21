out_file="query_01_results.csv"
query="
	\\copy (
	select
		n_name,
		sum(o_totalprice),
		c_custkey,
		date_part('month',o_orderdate)
		from customer, orders, region, nation
	where 	
		n_regionkey=r_regionkey
		AND c_nationkey=n_nationkey
		AND c_custkey=o_custkey
	group by
		n_name, date_part, c_custkey
	) 
	to '$out_file' with csv header
	;"


#mytime="$(time ( ls ) 2>&1 1>/dev/null )"
echo Executing query...	
mytime="$(time(echo $query | psql -U asap tpch) 2>&1) "
echo $mytime


