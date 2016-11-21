#!/usr/bin/env python
__author__ = 'cmantas'

from pyspark import SparkContext
from pyspark.mllib.clustering import KMeans
from numpy import array
from math import sqrt
from pyspark.mllib.linalg import SparseVector
from scipy.sparse import find


threads=4
inFile="myTFIDF"
local_output_filename="myTest.arff"

def myVec(line):
	from pyspark.mllib.linalg import SparseVector
	return  eval("SparseVector"+line)

#a dictionary that maps hashed indexes  to un-hashed ones (fake)
translator = {}
term_count = 0


def vector2dict(vec):
    """
    :param vec: a pyspark sparse vector
    :return: a list of dict of type  (index, value) representing the vector
    """
    global translator, term_count
    vector_dict = {}
    numpy_sparse = vec.toArray()
    rows, columns, values = find(numpy_sparse)
    for i in range(len(columns)):
        index = columns[i]
        value = values[i]
        # update a fake mapping of hashed indexes to non-hashed ones
        if index not in translator:
            translator[index] = term_count
            fake_index = term_count
            term_count += 1
        else:
            fake_index = translator[index]

        vector_dict[fake_index] = value
    return vector_dict


sc = SparkContext("local[%d]" % threads, "kmeans")

# Load and parse the data
data = sc.textFile(inFile).map(myVec)


# load data in local memory
data = data.collect()
# a list of lists of dict
tuple_data = []

all_elements = len(data)
consumed = 0

# convert all elements in data to dicts of (index-->value)
while consumed < all_elements:
    dict_vector = vector2dict(data[0])
    tuple_data.append(dict_vector)
    # try to free some memory
    del(data[0])
    consumed += 1


local_file = open(local_output_filename, 'w+')
local_file.write("@relation 'dummy TF/IDF'\n")

for i in range(term_count):
    local_file.write("@attribute dummy-%d numeric\n" % i )

local_file.write("\n\n@data\n\n")

for t in tuple_data:
    vector_string = "{"
    for index in t.keys():
        value = t[index]
        vector_string += str(index) + " " + str(value) + ","

    vector_string = vector_string[:-1]+"}"

    local_file.write(vector_string+"\n")

# close the output file
local_file.close()





