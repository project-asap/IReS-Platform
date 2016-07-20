from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext

import os
import sys

def main():
	conf = SparkConf().setAppName( "OutterSQL_Spark")
	sc = SparkContext( conf=conf)
	sqlContext = SQLContext(sc)

	host = sys.argv[ 1]
	yarn_home = sys.argv[ 2]
	sql_query = "".join( x for x in open( sys.argv[ 3]).readlines())
#	print( sql_query)
#	print( host, yarn_home)
	warehouse = "hdfs://" + host + ":9000/user/hive/warehouse"
	output = warehouse + "/finalParquet"

	lineitemParquet = sqlContext.read.format( 'parquet').options( header='true', inferschema='true').load( warehouse + '/lineitemparquet')
	lineitemParquet.registerTempTable( "lineitemParquet")
	partParquet = sqlContext.read.format( 'parquet').options( header='true', inferschema='true').load( warehouse + '/partParquet')
	partParquet.registerTempTable( "partParquet")
	part_aggParquet = sqlContext.read.format( 'parquet').options( header='true', inferschema='true').load( warehouse + '/part_aggParquet')
	part_aggParquet.registerTempTable( "part_aggParquet")

	os.system( yarn_home + "/bin/hdfs dfs -rm -r " + output)
	results = sqlContext.sql( sql_query)

	results.write.parquet( output)

if __name__ == "__main__":
	main()
