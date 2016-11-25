import sys
import os
import pickle
from sklearn.cluster import KMeans
from sklearn.externals import joblib

input=sys.argv[1]
output=sys.argv[2]
k=sys.argv[3]

tfidf = joblib.load(input)
km = KMeans(n_clusters=int(k))



print "Starting k-means training for k=",k
km = km.fit(tfidf)
with open(output,'wb') as handle:
	pickle.dump(km, handle)
print "k-means completed!"
