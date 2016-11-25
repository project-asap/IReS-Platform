import sys
import os
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer

input=sys.argv[1]
output=sys.argv[2]

files = os.listdir(input)
corpus = []

vectorizer = TfidfVectorizer(min_df=1)

for f in files:
	doc = open(input+f).readlines()
	for l in doc:
		corpus.append(l)

print "Doclument collection finished!"
print "Starting tf-idf transformation..."
tfidf = vectorizer.fit_transform(corpus)
with open(output,'wb') as handle:
	pickle.dump(tfidf, handle)
print "tf-idf transformation completed!"
