#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys

sys.path.append(os.path.abspath(os.getcwd()))

from ast import literal_eval
from sklearn.linear_model import SGDClassifier
import numpy as np
from utils import update_model, get_column_position, get_all_classes
import time
from sklearn.externals import joblib
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--classParam', required=True, help="parameters for classifier, in the python dictionary format")
parser.add_argument('--trainFile', required=True, help='training file')
parser.add_argument('--modelFile', required=True, help="stage 1 model folder")
parser.add_argument('--batchSize', required=True, type=int, help="divide input file by batch size")
args = parser.parse_args()


def train_model(train_file, classifier, batch_size):

    all_classes = get_all_classes(train_file)
    col_label, col_description = get_column_position(form=1)
    with open(train_file, "r") as input_file:

        first_line = input_file.readline().strip()
        entry = literal_eval(first_line)
        vector_size = len(entry[col_description])
        input_file.seek(0)  # reset the file pointer after reading first line 

        y_train = []
        X_train = np.full(shape=(batch_size, vector_size), fill_value=0, dtype=float)
        line_count = 0
        idx = 0
        start_time = time.time()
        for line in input_file:
            line_count += 1
            entry = literal_eval(line)
            y_train.append(entry[col_label])
            X_train[idx, :] = entry[col_description]

            if line_count % batch_size == 0:
                update_model(classifier, X_train, y_train, all_classes)
                print "training model for lines = ", line_count, 'time=', int(time.time() - start_time), 's'
                print "precision score", classifier.score(X_train, y_train)
                del y_train, X_train
                y_train = []
                X_train = np.full(shape=(batch_size, vector_size), fill_value=0, dtype=float)
                idx = -1

            idx += 1

        if line_count % batch_size > 1:
            X_train = X_train[:idx, :]
            update_model(classifier, X_train, y_train, all_classes)
            print "training model for lines = ", line_count, 'time=', int(time.time() - start_time), 's'
            print "precision score", classifier.score(X_train, y_train)


classifier = SGDClassifier(**literal_eval(args.classParam))
train_model(args.trainFile, classifier, args.batchSize)
joblib.dump(classifier, args.modelFile)

print "-" * 50 , "\n"
