SELECT	l_partkey AS agg_partkey, 
	0.2 * AVG(l_quantity) AS avg_quantity
FROM lineitemParquet
GROUP BY l_partkey
