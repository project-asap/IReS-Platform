import argparse
import sys
from sys import stdout

parser = argparse.ArgumentParser(description='runs kmeans on spark for .csv files')

parser.add_argument("-k","--K", help="the K parameter of K-means", type=int, required=True)
parser.add_argument("-mi","--max_iterations", help="the max iterations of the algorithm", type=int, required=True)
parser.add_argument("-i", "--input",  help="the input dir (RDD)", required=True)
args = parser.parse_args()

print "HELLO"

k = args.K
max_iter = args.max_iterations
fname = args.input
if not fname.startswith('/'):
    print "Please specify an absolute path for the input"
    exit()

from common import hdfs_master
fname = "hdfs://%s:9000/" % hdfs_master + fname

from pyspark import SparkContext
from pyspark.mllib.clustering import KMeans
from numpy import array
from math import sqrt
from pyspark.mllib.linalg import SparseVector


# init the spark context
if "sc" not in globals():
    sc = SparkContext( appName="K-Means")

def myVec(line):
	from pyspark.mllib.linalg import SparseVector
	return  eval("SparseVector"+line.strip())



# Load and parse the data
data = sc.textFile(fname+"/part*").map(myVec)
data.cache()


# Build the model (cluster the data)
clusters = KMeans.train(data, k, maxIterations=max_iter, runs=1, initializationMode="random")

print "--Available clustes count :"+str(len(clusters.clusterCenters)) 
#free space??
data.unpersist()


# # Evaluate clustering by computing Within Set Sum of Squared Errors
# def error(point):
#     center = clusters.centers[clusters.predict(point)]
#     return sqrt(sum([x**2 for x in (point - center)]))

# WSSSE = parsedData.map(lambda point: error(point)).reduce(lambda x, y: x + y)
# print("Within Set Sum of Squared Error = " + str(WSSSE))


f = open("spark_kmeans_centroids.out", "w+")
for c in clusters.clusterCenters:
    nzl = []
    for i in range(len(c)):
        if c[i]>=0.2:
            nzl.append((i, c[i]))
    f.write(str(nzl))
    f.write("\n\n")


f.close()

print "--OK--"
stdout.flush()
