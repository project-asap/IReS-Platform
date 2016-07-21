CREATE TABLE PART_AGG
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
AS 
	SELECT	l_partkey AS agg_partkey, 
			0.2 * AVG(l_quantity) AS avg_quantity
	FROM lineitem 
	GROUP BY l_partkey
;
