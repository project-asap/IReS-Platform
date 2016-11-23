CREATE TABLE FINAL_RESULTS
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
AS 
SELECT
	SUM(agg_extendedprice) / 7.0 AS avg_yearly
FROM
	part join part_agg
		on  p_partkey = agg_partkey
WHERE
	p_brand = 'Brand#33'
	AND p_container = 'MED BAG'
;
