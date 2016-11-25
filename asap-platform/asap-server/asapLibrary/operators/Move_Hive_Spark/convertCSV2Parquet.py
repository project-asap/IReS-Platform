from pyspark import SparkContext, SparkConf
from pyspark.sql import SQLContext, HiveContext
from pyspark.sql.types import *

import os
import sys
import argparse
import traceback
import subprocess

def main():

	#define command line arguments
	parser = argparse.ArgumentParser( description='Converts CSV files to Parquet and vice versa.')

	parser.add_argument( 'src', metavar='sources', type=str, nargs='+',
						 help='the directory where the files to be converted reside or a series of files'
						 		+ ' to be converted.\n\n')

	parser.add_argument( '--d', metavar='conversiondirection', type=str, nargs='*', choices=( 'parquet', 'csv'), default='parquet',
						 help='define which conversion will take place i.e. from csv to parquet or vice versa. By default,'
						 		+ ' the conversion is from csv to parquet. For the opposite conversion user should pass the' 
						 		+ ' the value "csv"\n\n')

	parser.add_argument( '--sep', metavar='separator', type=str, default=' ',
						 help='the column separator of each data file. Default -> " " i.e. space')

	parser.add_argument( '--out', metavar='output', type=str, nargs='?', default='.',
						 help='the output files will be saved in folder "converted" but the parent directory of this folder'
						 		+ ' can be altered by user through this flag. Default -> . i.e. the output path is the current working'
						 		+ ' directory\n\n')

	args = parser.parse_args()

	#results exportation
	path = args.out + "/converted"
	#if not os.path.exists( path):
	#	try: 
	#		os.makedirs( path)
	#	except OSError:
	#		if not os.path.isdir( path):
	#			raise
	#			exit( 1)

	#read data files
	#datafiles = []
	#for file in args.src:
		#check if it is a file or a directory
	#	if os.path.isdir( file):
	#		dfs = os.listdir( file)
			#file -> directory, f -> actual file
	#		dfs = [ file.rstrip( "/") + "/" + f for f in dfs if not os.path.isdir( f)]
	#		datafiles = datafiles + dfs
	#	else:
	#		datafiles.append( file)


	fileSchemas = { "customer": StructType([	StructField( "c_custkey", IntegerType(), True),
    						  					StructField( "c_name", StringType(), True),
											  	StructField( "c_address", StringType(), True),
					  						  	StructField( "c_nationkey", DecimalType( 38, 0), True),
					  						  	StructField( "c_phone", StringType(), True),
					  						  	StructField( "c_acctbal", DecimalType( 10, 2), True),
					  						  	StructField( "c_mktsegment", StringType(), True),
					  						  	StructField( "c_comment", StringType(), True)]),
      				"lineitem": StructType([	StructField( "l_orderkey",DecimalType( 38, 0), True),
											  	StructField( "l_partkey", DecimalType( 38, 0), True),
											  	StructField( "l_suppkey", DecimalType( 38, 0), True),
					  						  	StructField( "l_linenumber", IntegerType(), True),
					  						  	StructField( "l_quantity", DecimalType( 10, 2), True),
					  						  	StructField( "l_extendedprice", DecimalType( 10, 2), True),
					  						  	StructField( "l_discount", DecimalType( 10, 2), True),
					  						  	StructField( "l_tax", DecimalType( 10, 2), True),
					  						  	StructField( "l_returnflag",StringType(), True),
					  						  	StructField( "l_linestatus", StringType(), True),
					  						  	StructField( "l_shipdate", DateType(), True),
					  						  	StructField( "l_commitdate", DateType(), True),
					  						  	StructField( "l_receiptdate", DateType(), True),
					  						  	StructField( "l_shipinstruct", StringType(), True),
					  						  	StructField( "l_shipmode", StringType(), True),
					  						  	StructField( "l_comment", StringType(), True)]),
      				"nation":   StructType([ 	StructField( "n_nationkey", IntegerType(), True),
											  	StructField( "n_name", StringType(), True),
											  	StructField( "n_regionkey", DecimalType( 38, 0), True),
											  	StructField( "n_comment", StringType(), True)]),
					"orders":   StructType([	StructField( "o_orderkey", IntegerType(), True),
											  	StructField( "o_custkey", DecimalType( 38, 0), True),
											  	StructField( "o_orderstatus", StringType(), True),
					  						  	StructField( "o_totalprice", DecimalType( 10, 2), True),
					  						  	StructField( "o_orderdate", DateType(), True),
					  						  	StructField( "o_orderpriority", StringType(), True),
					  						  	StructField( "o_clerk", StringType(), True),
					  						  	StructField( "o_shippriority", IntegerType(), True),
					  						  	StructField( "o_comment", StringType(), True)]),
      				"part":     StructType([	StructField( "p_partkey", IntegerType(), True),
											  	StructField( "p_name", StringType(), True),
											  	StructField( "p_mfgr", StringType(), True),
					  						  	StructField( "p_brand", StringType(), True),
					  						  	StructField( "p_type", StringType(), True),
					  						  	StructField( "p_size", IntegerType(), True),
					  						  	StructField( "p_container", StringType(), True),
					  						  	StructField( "p_retailprice", DecimalType( 10, 2), True),
					  						  	StructField( "p_comment", StringType(), True)]),
      				"partsupp": StructType([	StructField( "ps_partkey", DecimalType( 38, 0), True),
											  	StructField( "ps_suppkey", DecimalType( 38, 0), True),
											  	StructField( "ps_availqty", IntegerType(), True),
					  						  	StructField( "ps_supplycost", StringType(), True),
					  						  	StructField( "ps_comment", StringType(), True)]),
      				"region":   StructType([	StructField( "r_regionkey", IntegerType(), True),
											  	StructField( "r_name", StringType(), True),
											  	StructField( "r_comment", StringType(), True)]),
					"supplier":	StructType([	StructField( "s_suppkey", IntegerType(), True),
                                              						  	StructField( "s_name", StringType(), True),
                                                                                                StructField( "s_address", StringType(), True),
					  						  	StructField( "s_nationkey", DecimalType( 38, 0), True),
					  						  	StructField( "s_phone", StringType(), True),
					  						  	StructField( "s_acctbal", DecimalType( 10, 2), True),
					  						  	StructField( "s_comment", StringType(), True)]),
      				"part_agg":   StructType([	StructField( "agg_partkey", DecimalType( 38, 0), True),
                                                                StructField( "avg_quantity", DecimalType( 10, 2), True),
                                                                StructField( "agg_extendedprice", DecimalType( 10, 2), True)])
    			  }		  
    			  
	conf = SparkConf().setAppName( "Convert CSV to Parquet")
	sc = SparkContext(conf=conf)
	sqlContext = SQLContext(sc)
        namenode = "hdfs://master:9000"
        warehouse = "/user/hive/warehouse"
        print( args.src)
        forschema = args.src[ 0].split( "/")[-1].split( ".")[0]
        print( forschema)
        output = args.src[ 0] + ".parquet"
        inputdir = warehouse + "/" + args.src[ 0]
        fnames = subprocess.check_output( "/opt/hadoop-2.7.0/bin/hdfs dfs -ls " + inputdir, shell = True)
        fnames = [ x for x in fnames.split() if x.startswith( warehouse)]
        for file in fnames:
                print( "FILE IS:", file)
                fileSchema = fileSchemas[ forschema]
                print( "FILESCHEMA IS ", fileSchema)
                df = sqlContext.read.format( "com.databricks.spark.csv").options( header="false", delimiter="|", inferSchema="true").load( file, schema=fileSchema)
                df.printSchema()
                try:
                    df.write.format( "parquet").save( namenode + warehouse + "/" + output, mode="append")
                except Exception:
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    formatted_lines = traceback.format_exc().splitlines()
                    print(formatted_lines[0])
                    print(formatted_lines[-1])
                    print("*** format_exception:")
                    print(repr(traceback.format_exception(exc_type, exc_value, exc_traceback)))
                    pass

if __name__ == "__main__":
	main()
