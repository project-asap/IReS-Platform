from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext

import os
import sys

def main():
	conf = SparkConf().setAppName( "InnerSQL_Spark")
	sc = SparkContext( conf=conf)
	sqlContext = SQLContext(sc)

	host = sys.argv[ 1]
	yarn_home = sys.argv[ 2]
	sql_query = "".join( x for x in open( sys.argv[ 3]).readlines())
#	print( sql_query)
#	print( host, yarn_home)
	warehouse = "hdfs://" + host + ":9000/user/hive/warehouse"
	output = warehouse + "/part_aggParquet"

	lineitemParquet = sqlContext.read.format( 'parquet').options( header='true', inferschema='true').load( warehouse + '/lineitemparquet')
	lineitemParquet.registerTempTable( "lineitemParquet")

	os.system( yarn_home + "/bin/hdfs dfs -rm -r " + output)
	results = sqlContext.sql( sql_query)

	results.write.parquet( output)

if __name__ == "__main__":
	main()
