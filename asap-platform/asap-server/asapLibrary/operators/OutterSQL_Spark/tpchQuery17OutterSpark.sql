SELECT 	SUM(agg_extendedprice) / 7.0 AS avg_yearly
FROM 	
	part,
	part_agg
WHERE	
	p_brand = 'Brand#33'
	AND p_container = 'MED BAG'
