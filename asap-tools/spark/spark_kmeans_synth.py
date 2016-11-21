
import argparse
from getpass import getuser
from sys.stdout import flush

parser = argparse.ArgumentParser(description='runs kmeans on spark for .csv files')

parser.add_argument("-k","--K", help="the K parameter of K-means", type=int, required=True)
parser.add_argument("-i","--max_iterations", help="the max iterations of the algorithm", type=int, required=True)
parser.add_argument("--file", '-f', help="the input file (local)", )
parser.add_argument("--distributed_file", '-df', help="the input file in HDFS", )
parser.add_argument('-d', action='store_true', default=False)
args = parser.parse_args()

if args.distributed_file is not None:
    dfs = True
    in_fname =  args.distributed_file
    if not in_fname.startswith("/"):
        in_fname = "/user/"+getuser()+"/"+in_fname
    fname = "hdfs://localhost:9000/"+ in_fname
elif args.file is not None:
    dfs = False
    fname = args.file

k = args.K
max_iter = args.max_iterations

dfs = args.d

from pyspark import SparkContext
from pyspark.mllib.clustering import KMeans
from numpy import array
from math import sqrt


####### RUNS ???? #######
runs = 10 #how many parallel runs
threads = 8


sc = SparkContext("local[%d]" % threads, "kmeans")


# Load and parse the data
data = sc.textFile(fname)
parsedData = data.map(lambda line: array([float(x) for x in line.split(',')]))

# Build the model (cluster the data)
clusters = KMeans.train(parsedData, k, maxIterations=max_iter, runs=runs, initializationMode="random")

# # Evaluate clustering by computing Within Set Sum of Squared Errors
# def error(point):
#     center = clusters.centers[clusters.predict(point)]
#     return sqrt(sum([x**2 for x in (point - center)]))

# WSSSE = parsedData.map(lambda point: error(point)).reduce(lambda x, y: x + y)
# print("Within Set Sum of Squared Error = " + str(WSSSE))

for c in clusters.clusterCenters:
    print c


print "--OK--"
flush()
