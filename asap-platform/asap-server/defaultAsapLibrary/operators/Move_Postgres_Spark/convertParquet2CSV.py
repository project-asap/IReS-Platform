from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext
from pyspark.sql.types import *

import os
import sys

def main():
	conf = SparkConf().setAppName( "Convert Parquet2CSV")
	sc = SparkContext( conf=conf)
	sqlContext = SQLContext(sc)

	yarn_home = sys.argv[ 1]
        file = sys.argv[ 2]
	print( yarn_home, file)
        namenode = "hdfs://master:9000"
        warehouse = "/user/hive/warehouse"
        df = sqlContext.read.parquet( namenode + warehouse + "/" + file + ".parquet")
        df.printSchema()
#		for row in df.collect():
#			print( row)
	output = warehouse + "/" + file + ".csv"

        if os.path.exists( namenode + output):
            try:
                os.system( "/opt/hadoop-2.7.0/bin/hdfs dfs -rm -r " + output)
            except OSError:
                raise

        df.write.format( "com.databricks.spark.csv").options( delimeter=",").save( namenode  + output)

if __name__ == "__main__":
	main()
