"""
Classification in Spark
@author: Chris Mantas
@contact: the1pro@gmail.com
@since: Created on 2016-02-12
@todo: custom formats, break up big lines
@license: http://www.apache.org/licenses/LICENSE-2.0 Apache License
"""

from pyspark.mllib.classification import \
    LogisticRegressionWithLBFGS, LogisticRegressionModel
from imr_tools import *
from pyspark import SparkContext
from argparse import ArgumentParser
from os import system

# ==================== Global Vars ============================ #
_INTERCEPT = True
_REGULARIZATION = None


# ====================  Helper Functions ====================== #

def train_model(training_rdd, **kwargs):
    """
    Train a classifier model using  an rdd training dataset
    :param training_rdd: the rdd of the training dataset
    :param kwargs: additional key-value params for the training (if any)
    :return:
    """
    return LogisticRegressionWithLBFGS.train(training_rdd,
                                             regType=_REGULARIZATION,
                                             intercept=_INTERCEPT,
                                             **kwargs)


def get_cli_args():
    """
    Defines the command-line arguments, parses the input and returns a
    namespace object with the parameters given by the user
    :return:
    """
    cli_parser = ArgumentParser(description='Classification on Spark')
    cli_parser.add_argument("operation",
                            help="the operation to run: 'train' or 'classify'")
    cli_parser.add_argument("input",
                            help="the input dataset (formatted as a tuple)")
    cli_parser.add_argument("-m", '--model', required=True,
                            help="a csv file holding the model weights")
    cli_parser.add_argument("-o", '--output',
                            help="the output location "
                                 "(in case of classify op)")
    cli_parser.add_argument("-l", '--labels', required=True,
                            help="a json file holding all labels in a dataset")
    cli_parser.add_argument("-c", '--category', type=int, default=1,
                            help="which category (label) to use [1-3]")
    cli_parser.add_argument("-u", "--update", action='store_true',
                            default=False, help="update a pre-existing model")
    cli_parser.add_argument("-e", "--evaluate", action='store_true',
                            default=False,
                            help="cross-evaluate the model on 20%% of input")

    return cli_parser.parse_args()


def get_spark_context(appname="spark_job"):
    # ----------------  Start Spark Job ----------------  #
    print("=============>  {}  <=============".format(appname))
    if "sc" not in globals():  # init the spark context
        print("---> Init. Spark Context")
        sc = SparkContext(appName=appname)
        print("---> OK")
    return sc


def calculate_error(valid_rdd, model):
    """
    Calculate the error ratio of a given model on a given RDD dataset
    :param valid_rdd: an RDD of LabeledPoints
    :param model: a classification model
    :return: a float in the [0-1] range
    """
    label_and_preds = valid_rdd.map(
            lambda p: (p.label, model.predict(p.features))
            )
    erroneous = label_and_preds.filter(lambda (l, p): l != p)
    return erroneous.count() / float(valid_rdd.count())


# ====================  Spark Jobs ====================== #

def perform_train_job(sc, input_path, l_encoder,
                      initial_model=None, evaluate=False, category=1):
    """
    Trains a Linear Regression model and returns its weights
    :param input_path: the input file-name (local or HDFS)
    :param l_encoder: The label encoder
    :param initial_model: the initial LR model we will be enhancing
    :param evaluate: Whether or not to cross-evaluate the model on a 20% split
    :param category: the label category
    :return: a list of model weights for a Logistic Regression Model
    """

    tuple_data = sc.textFile(input_path).map(parse_line)  # an RDD of tuples

    # an RDD of Labeled Points (with encoded labels)
    all_data = tuple_data.map(
            lambda e: tuple_to_labeled_point(e, category, l_encoder))

    # choose the training data
    if evaluate:
        # split the dataset in training and validation sets
        (training_data, validation_data) = all_data.randomSplit([0.8, 0.2])
    else:
        # use the whole dataset
        training_data = all_data

    if initial_model:
        print("---> Updating Classification Model")
    else:
        print("---> Training Classification Model")

    # ----------------   Do the training ----------------  #
    weights = initial_model.weights if initial_model else None
    model = train_model(training_data, numClasses=len(l_encoder.classes_),
                        initialWeights=weights)
    print("   > Done")

    # calculate training error
    print("---> Calculating Training Error")
    error = calculate_error(training_data, model)
    print("   > "+str(error))

    # if evaluate is given, calculate the evaluation error too
    if evaluate:
        print("---> Calculating Evaluation Error")
        error = calculate_error(validation_data, model)
        print("   > "+str(error))

    return model


def perform_classification_job(sc, input_path, encoder, model,
                               output_path):
    """
    Classifies the entries of a dataset based on a model created from the
    weights given as an input
    :param input_path: the input (text) dataset location
    :param encoder: The label encoder for this category
    :param model: The LR model

    :param output_path: The output dataset location
    :return: None
    """

    raw_data = sc.textFile(input_path)  # the raw text input RDD

    # A parser function.
    # It r just needs to take the features from a text line
    parsing_mapper_func = get_features_from_line

    # an RDD of feature vectors
    print("---> Parsing Input")
    feature_entries = raw_data.map(parsing_mapper_func)
    print("---> OK")

    def classifying_func(features):
        """
        A closure (function) using classify_line and the model and encoder
         that are available in this scope
        :param features:
        :return:
        """
        return classify_line(features, model, encoder)

    # ---------------- Do the classification ---------------- #
    print("---> Classifying.... the Input!!")
    labeled_entries = feature_entries.map(classifying_func)
    print("   > Done")

    # save the output as a text file
    print("SAVING!")
    labeled_entries.saveAsTextFile(output_path)


# =======================  MAIN ========================= #
if __name__ == "__main__":

    # get the command-line arguments
    args = get_cli_args()

    # create a label encoder from a local json file that contains the set
    l_encoder = label_encoders_from_json_file(args.labels, args.category)

    # ---------------  Choose the Operation to perform =-------------- #
    if args.operation.lower() == "train":
        # get/create spark context
        sc = get_spark_context("Train/Update LR Model")

        # load initial weights if it's an update operation
        init_model = None
        if args.update:
            print("---> Loading model")
            LogisticRegressionModel.load(sc, args.model)
            print("---> OK")

        # do the train job
        model = perform_train_job(sc, args.input, l_encoder,
                                          initial_model=init_model,
                                          evaluate=args.evaluate,
                                          category=args.category)
        # save the model weights as a csv file
        try:
            system("hdfs dfs -rm -r " + args.model)
        except:
            print "Failed to delete model: ", args.model
        print("---> Saving LR model")
        model.save(sc, args.model)
        print("---> OK")

    elif args.operation.lower() == "classify":
        if not args.output:
            raise Exception("for classify operation, an output needs to be"
                            "specified")
        sc = get_spark_context("Classify (Logistic Regression)")

        # load the model
        print("---> Loading model")
        model = LogisticRegressionModel.load(sc, args.model)
        print("---> OK")

        # do the classification job
        perform_classification_job(sc, args.input, l_encoder,
                                   model, args.output)
    else:
        print("I do not know operation: "+args.operation)
