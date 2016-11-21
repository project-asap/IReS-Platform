import argparse
from os import system
from sys import stdout

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
from pyspark.mllib.feature import HashingTF, IDF, RDD


class myHashingTF(HashingTF):
    def create_dic(self, document_terms):
        if not isinstance(document_terms, RDD):
            raise Exception("We need an RDD as input")
        #create hashes and reduce by key
        dict = document_terms.flatMap(lambda terms: [(t, self.indexOf(t)) for t in terms]).reduceByKey(lambda a, b: a)
        return dict

def filter_and_split(text):
    delims = u"\r\n\t.,;:'\"()?!$#-0123456789/*%<>@[]+`~_=&^ "
    translate_table = dict((ord(char), u" ") for char in delims)
    return text.lower().strip().translate(translate_table).split(" ")


# init the spark context
if "sc" not in globals():
    sc = SparkContext( appName="TF-IDF")

# Load documents (one per line).
documents = sc.sequenceFile(docs_dir).map(lambda (fname, content): filter_and_split(content))
documents.cache()

# # keep only the content (replace, lower, split, etc)
# documents = documents.

hashingTF = myHashingTF()


# create the tf vectors
tf = hashingTF.transform(documents)
# create the idf vectors
idf = IDF().fit(tf)
tfidf = idf.transform(tf)
#save
tfidf.saveAsTextFile(d_out)

# create and save the dictionary rdd
dict = hashingTF.create_dic(documents)
dict.saveAsSequenceFile(d_out+"/dictionary")



# free space?
dict.unpersist()
tfidf.unpersist()
#documents.unpersist()
print "--OK--"
stdout.flush()
