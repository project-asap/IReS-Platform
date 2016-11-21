__author__ = 'cmantas'

import argparse
parser = argparse.ArgumentParser(description='runs kmeans on spark for .csv files')
parser.add_argument('operation', help='"train" or "classify"')
parser.add_argument("-it","--iterations", help="the max iterations of the algorithm", type=int, default=3)
parser.add_argument("-i", "--input",  help="the input data path", required=True)
parser.add_argument("-mf", "--model-file",  help="path of the model file", required=True)
args = parser.parse_args()

from re import split
from pyspark import SparkContext, SparkConf
from pyspark.mllib.classification import SVMWithSGD, SVMModel
from pyspark.mllib.regression import LabeledPoint
from common import *



def parse_point(line):
    """
    Loads and parses a line of data
    :param line:
    :return:
    """
    values = [float(x) for x in split(' |,|;', line)]
    return LabeledPoint(values[0], values[1:])


def train_model(training_data, iterations, model_file_path, calculate_error=True):
    """
    Trains an SVG model and saves it
    :param training_data:
    :param iterations:
    :param model_file_path:
    :return:
    """
    parsed_data = sc.textFile(training_data).map(parse_point)

    # Build the model
    model = SVMWithSGD.train(parsed_data, iterations=iterations)

    # Save the model
    model.save(sc, model_file_path)
    print "Model saved in: ", model_file_path

    if calculate_error:
        #predictions
        labelsAndPreds = parsed_data.map(lambda p: (p.label, model.predict(p.features)))
        trainErr = labelsAndPreds.filter(lambda (v, p): v != p).count() / float(parsed_data.count())
        print("============Training Error = " + str(trainErr))


def classify_with_model(input_data_path, model_file_path):
    input_parsed = sc.textFile(input_data_path).map(parse_point)
    model = SVMModel.load(sc, model_file_path)
    labels = input_parsed.map(lambda p: model.predict(p.features))
    labels.saveAsTextFile("predictions")



if __name__ == "__main__":

    model_file_path = to_hdfs_url(args.model_file)
    operation = args.operation

    # choose the operation
    if operation.lower() == "train":
        training_data = to_hdfs_url(args.input)
        iterations = args.iterations
        # init the spark context
        if "sc" not in globals():
            print "=== INIT sc"
            conf = SparkConf()
            conf.set("spark.hadoop.validateOutputSpecs", "false")
            sc = SparkContext( appName="SVM Train", conf=conf)
            print "=== TRAINING"
            train_model(training_data, iterations, model_file_path)
            print "=== TRAINED"
    elif args.operation.lower() == "classify":
        input_file_path = to_hdfs_url(args.input)
        # init the spark context
        if "sc" not in globals():
            conf = SparkConf()
            conf.set("spark.hadoop.validateOutputSpecs", "false")
            sc = SparkContext( appName="SVM Classification", conf=conf)
        classify_with_model(input_file_path, model_file_path)
    else:
        print "I do not know operation '%s'" % args.operation


