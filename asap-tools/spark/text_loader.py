__author__ = 'cmantas'

import argparse

from os import system

parser = argparse.ArgumentParser(description='runs TF/IDF on a directory of text docs')
# parser.add_argument("-o","--operation", help="Operation: Load or Save",  required=True)
parser.add_argument("-i","--input", help="the local input directory",  required=True)
parser.add_argument("-do", "--distributed_output",  help="the output file in HDFS",  required=False)
# parser.add_argument("-t", "--type", help="the output file format, seq or text", required=False)
args = parser.parse_args()

docs_dir = args.input
d_out = args.distributed_output

system("hdfs dfs -rm -r %s"%d_out)

d_out = "hdfs://master:9000/" + d_out

from pyspark import SparkContext, RDD, SparkConf
conf = SparkConf().setAppName("Move").__setattr__("spark.hadoop.validateOutputSpecs", "false")

sc = SparkContext("local", appName="Text-Loader")




# if args.operation not in ["save", "load"]:
#     raise Exception("Wrong args")
#     exit()

# if "save" in args.operation:
#     if args.type not in ["text", "seq"]:
#         raise Exception("The output type can be either seq(Sequence file) or text(Text file)")
#         exit()





documents = sc.wholeTextFiles(docs_dir)
documents.saveAsSequenceFile(d_out)

# else:
#     seq_file = sc.sequenceFile("hdfs://master:9000/"+args.input)
#     text = seq_file.collect()
#     for line in text:
# 	print line[1].strip()
