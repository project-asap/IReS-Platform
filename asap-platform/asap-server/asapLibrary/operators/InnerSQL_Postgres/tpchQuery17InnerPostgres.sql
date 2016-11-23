DROP TABLE IF EXISTS part_agg; 
CREATE TABLE part_agg
AS 
	SELECT	l_partkey AS agg_partkey, 
			0.2 * AVG(l_quantity) AS avg_quantity
	FROM lineitem 
	GROUP BY l_partkey
;
