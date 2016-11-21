import argparse
from os import system

### args parsing
parser = argparse.ArgumentParser(description='runs TF/IDF on a directory of text docs')
parser.add_argument("-i","--input", help="the input in HDFS",  required=True)
parser.add_argument("-o", '--output', help="the output in  HDFS", required=True )
parser.add_argument("-mdf", '--min_document_frequency', default=1 )
args = parser.parse_args()

docs_dir = args.input
if not docs_dir.startswith('/'):
    print "Please specify an absolute path for the input"
    exit(-2)

# create hdfs paths
from common import to_hdfs_url

docs_dir = to_hdfs_url(docs_dir)
d_out = to_hdfs_url(args.output)
min_df = int(args.min_document_frequency)

# remove any previous output (is there a way to it from spark?)
#system("hdfs dfs -rm -r %s" % d_out)

# import spark-realated stuff
from pyspark import SparkContext
from pyspark.mllib.feature import HashingTF, IDF

# init the spark context
if "sc" not in globals():
    sc = SparkContext( appName="TF-IDF")

# Load documents (one per line).
documents = sc.sequenceFile(docs_dir)

#keep only the content
documents = documents.map(lambda (fname, content): content.split(" "))

hashingTF = HashingTF()
tf = hashingTF.transform(documents)


# IDF
idf = IDF().fit(tf)
tfidf = idf.transform(tf)

#save
tfidf.saveAsTextFile(d_out)

# free space?
tfidf.unpersist()
documents.unpersist()