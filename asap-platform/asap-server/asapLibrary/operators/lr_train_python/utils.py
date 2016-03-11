#!/usr/bin/python
# -*- coding: utf-8 -*-
from ast import literal_eval

import nltk
from bs4 import BeautifulSoup
import re
import unicodedata
import time
import codecs
from config import *
import itertools
import numpy as np

stopwords = []
with open(path_stopword_1, "r") as f:
    stopwords += f.read().split('\n')

with open(path_stopword_2, "r") as f:
    stopwords += f.read().split('\n')

stopwords += nltk.corpus.stopwords.words(input_language)

stopwords += additional_stopwords
stopwords = set(stopwords)

# stemmer = Stemmer.Stemmer('french')
stemmer = nltk.stem.SnowballStemmer(input_language)


def header(form=0):
    if form == 1:
        columns = [ID_Product, category_1, category_3, header_description]
    elif form == 0:
        columns = [ID_Product, category_1, category_2, category_3, header_description, header_label, header_brand,
                   Produit_Cdiscount, header_price]
    return columns


def normalize_txt(txt):
    # remove html stuff
    txt = BeautifulSoup(txt, from_encoding='utf-8').get_text()
    # lower case
    txt = txt.lower()
    # special escaping character '...'
    txt = txt.replace(u'\u2026', '.')
    txt = txt.replace(u'\u00a0', ' ')
    # remove accent btw
    txt = unicodedata.normalize('NFD', txt).encode('ascii', 'ignore')
    # txt = unidecode(txt)
    # remove non alphanumeric char
    txt = re.sub('[^a-z_]', ' ', txt)
    # remove french stop words
    tokens = [w for w in txt.split() if (len(w) > 2) and (w not in stopwords)]
    # french stemming
    tokens = [stemmer.stem(token) for token in tokens]
    #    tokens = stemmer.stemWords(tokens)
    return ' '.join(tokens)


def normalize_file(fname, ofname, header, nrows=None):
    columns = {k: v for v, k in enumerate(header)}
    di = columns[header_description]
    li = columns[header_label]
    mi = columns[header_brand]
    cat1 = columns[category_1]
    cat3 = columns[category_3]
    prodID = columns[ID_Product]
    ff = codecs.open(ofname, 'w', encoding='utf-8')
    start_time = time.time()
    counter = 0
    for line in open(fname):
        # if error occurs in this part, verify that line.split(csv_delimiter) has the same number of elements for every lines
        if line.startswith(ID_Product):
            continue
        if counter % count_line == 0:
            print fname, 'normalizing lines=', counter, 'time=', int(time.time() - start_time), 's'
        ls = line.split(csv_delimiter)
        # marque normalization
        txt = ls[mi]
        txt = re.sub('[^a-zA-Z0-9]', '_', txt).lower()
        ls[mi] = txt
        # description normalization
        ls[di] = normalize_txt(ls[di])
        # libelle normalization
        ls[li] = normalize_txt(ls[li])
        eles = [ls[prodID], ls[cat1], ls[cat3], (ls[mi] + " ") * 3 + (ls[li] + " ") * 2 + ls[di]]
        line = csv_delimiter.join(eles)
        ff.write(line + u'\n')
        counter += 1
        if (nrows is not None) and (counter >= nrows):
            break
    ff.close()
    return


def word2vec_txt(txt, model, embed_size):
    ws = txt.split()
    lv = []
    for w in ws:
        try:
            v = model[w]
        except:
            v = np.zeros(embed_size)
        lv.append(v)
    n = len(lv)
    vf = [sum(sublist) / n for sublist in itertools.izip(*lv)]
    return vf


def load_and_vectorize_data(ifname, ofname, w2vmodel, embed_size):
    outFile = codecs.open(ofname, "w", encoding='utf-8')
    start_time = time.time()
    counter = 0
    with codecs.open(ifname, "r", encoding='utf-8') as f:
        for line in f:
            ls = line.split(csv_delimiter)
            vect = word2vec_txt(ls[-1], w2vmodel, embed_size)
            outLine = ",".join(ls[:-1]) + ",[" + ",".join(str(ele) for ele in vect) + "]\n"
            outFile.write(outLine)
            counter += 1
            if counter % count_line == 0:
                print 'vectorizing lines = ', counter , '  time=', int(time.time() - start_time), "s"
    outFile.close()

def get_column_position(form=1):
    colnames = header(form=1)
    columns = {k: v for v, k in enumerate(colnames)}
    col_description = columns[header_description]
    col_label = columns[category_3]
    return col_label, col_description

def get_all_classes(fname):
    col_label, col_description = get_column_position(form=1)
    ls_labels = []
    with open(fname, "r") as f:
        for line in f:
            entry = literal_eval(line)
            ls_labels.append(entry[col_label])
    return list(set(ls_labels))


def update_model(classifier, X, y, all_classes):
    classifier.partial_fit(X, y, classes=all_classes)

