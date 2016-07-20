SELECT 	SUM(l_extendedprice) / 7.0 AS avg_yearly
FROM 	lineitemParquet,
		partParquet,
		part_aggParquet
WHERE	p_partkey = l_partkey
		AND agg_partkey = l_partkey
		AND p_brand = 'Brand#33'
		AND p_container = 'MED BAG'
		AND l_quantity < avg_quantity
LIMIT 1;
