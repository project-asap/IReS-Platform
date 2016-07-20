DROP TABLE IF EXISTS INTERMEDIATE_RESULTS; 
CREATE TABLE INTERMEDIATE_RESULTS
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
AS 
	SELECT l_orderkey
	FROM lineitem
	GROUP BY l_orderkey
	HAVING SUM(l_quantity) > 313
;
