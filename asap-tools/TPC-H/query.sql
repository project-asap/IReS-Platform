select
	n_name,
	sum(o_totalprice),
	date_part('month',o_orderdate)
from customer, orders, region, nation
where 	
	n_regionkey=r_regionkey
	AND c_nationkey=n_nationkey
	AND c_custkey=o_custkey
group by
	n_name, date_part, c_custkey
;

