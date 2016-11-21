# -*- coding:utf-8 -*-
__author__ = 'Victor Giannakouris Salalidis'

from gensim import models, corpora, matutils, similarities
import numpy as np
import scipy.stats as stats
import json
import sys, os.path, time

class ModelTrainer():
    def __init__(self, input_documents=None, working_dir=None):
        self.input_documents = input_documents


    """
    LDA(Latent Dirichlet Allocation) training
    """
    def train(self, number_of_topics, iterations, input_docs=None):
        if input_docs:
            data = input_docs
            if not os.path.isfile(data):
                raise IOError("File '%s' does not exist" % str(data))
        else:
            data = os.listdir(self.input_documents)


        text_data = []
        stop_words = []
        for d in data:
            with open(self.input_documents+'/'+d) as inFile:
                text_data.append(unicode(inFile.read(), errors='ignore'))

        tokenized_texts = [[word for word in doc
                            if (word not in stop_words)] for doc in text_data]

        self.my_corpus = tokenized_texts



        self.dictionary = corpora.Dictionary(tokenized_texts)
        self.corpus = [self.dictionary.doc2bow(text) for text in tokenized_texts]


        self.model = models.LdaModel(num_topics=number_of_topics,
                                     corpus=self.corpus,
                                     id2word=self.dictionary,
				     iterations=iterations)

    """
    Saves the model and it's
    dictionary for future use
    """
    def save(self):
        self.model.save("Analysis/ModelData/lda")
        self.dictionary.save("Analysis/ModelData/dictionary")

    """
    Loads a saved model and it's dictionary
    """
    def load(self, lda_path, dictionary_path):
        model = models.LdaModel.load(lda_path)
        self.model = model
        self.dictionary = corpora.Dictionary.load(dictionary_path)

if __name__ == "__main__":
    path = sys.argv[1]
    k = int(sys.argv[2])
    iterations = int(sys.argv[3])
    model = ModelTrainer(input_documents = path)
    print "Start training..."
    start = time.time()
    model.train(k, iterations=iterations)
    
    operator = "LDA_Gensim"
    execTime = time.time() - start
    n = len(os.listdir(path))

    out = dict({"operator": operator, "exec_time": execTime, "k": k, "size": n})
    print "\n\n LDA Gensim Completed\n\n"
    print out
