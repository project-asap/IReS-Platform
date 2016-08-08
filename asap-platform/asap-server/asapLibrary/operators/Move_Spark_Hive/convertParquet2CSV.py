from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext
from pyspark.sql.types import *

import os
import sys
<<<<<<< HEAD
=======
import subprocess
>>>>>>> 679b7257e992f967a6c90fdd205f40a21e7f2014

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
<<<<<<< HEAD
#		for row in df.collect():
#			print( row)
	output = warehouse + "/" + file + ".csv"

        if os.path.exists( namenode + output):
            try:
                os.system( "/opt/hadoop-2.7.0/bin/hdfs dfs -rm -r " + output)
            except OSError:
                raise

        df.write.format( "com.databricks.spark.csv").options( delimiter='|').save( namenode  + output)

if __name__ == "__main__":
	main()
=======
	output = warehouse + "/" + file + ".csv"

#        if os.path.exists( namenode + output):
#            try:
#                os.system( "/opt/hadoop-2.7.0/bin/hdfs dfs -rm -r " + output)
#            except OSError:
#                raise

        df.write.format( "com.databricks.spark.csv").options( delimiter='|').save( namenode  + output)
        #merge output file to one
        #fnames = subprocess.check_output( "/opt/hadoop-2.7.0/bin/hdfs dfs -ls " + output, shell = True)
        #fnames = [ x for x in fnames.split() if x.startswith( warehouse) and not x.endswith( "_SUCCESS")]
        #for file_part in fnames:
         #   print( "FILE IS:", file_part)
            #forschema = file.split( "/")[-1].split( ".")[0]
            #fileSchema = fileSchemas[ forschema]
            #print( "FILESCHEMA IS ", fileSchema)
          #  df = sqlContext.read.format( "com.databricks.spark.csv").options( header="false", delimiter="|", inferSchema="true").load( file_part)
           # df.printSchema()
            #df.write.format( "com.databricks.spark.csv").save( output + "/" + file + ".csv", mode = "append")

if __name__ == "__main__":
    main()
>>>>>>> 679b7257e992f967a6c90fdd205f40a21e7f2014
