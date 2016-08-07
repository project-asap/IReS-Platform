#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
tools for imr datasets
@author: Chris Mantas
@contact: the1pro@gmail.com
@since: Created on 2016-02-12
@todo: custom formats, break up big lines
@license: http://www.apache.org/licenses/LICENSE-2.0 Apache License
"""

from ast import literal_eval
from collections import defaultdict


def create_label_encoder(labels):
    """
    Creates a label encoder from a list of labels
    :param labels: a list of integers
    :return: a LabelEncoder object
    """
    from sklearn.preprocessing import LabelEncoder
    encoder = LabelEncoder()
    encoder.fit(labels)
    return encoder


def get_features_from_line(line):
    """
    Given a text line it returns
     a) only the last element of the tuple if the line is a tuple.
        That element we assume to be a list of features.
     b) the line's elements if the line is not a tuple
    :param line:
    :return:
    """
    from ast import literal_eval
    entry = literal_eval(line)
    return entry[-1] if isinstance(entry, tuple) else entry


def parse_line(line):
    """
    Parses a string line to a tuple
    :param line:
    :return:
    """
    from ast import literal_eval
    try:
        entry = literal_eval(line)
        if not isinstance(entry, tuple):
            raise Exception("Input parsed, but is not a tuple")
    except:
        raise Exception("Could not evaluate (parse) input into an object")
    return entry


def tuple_to_labeled_point(entry, category, l_encoder=None):
    """
    Creates a label point from a text line that is formated as a tuple
    :param entry: a tuple of format (3, 2, 1, [3,4,4 ..]), where the first
            entries in the tuple are labels, and the last entry is
            a list of features
    :param category: which one of the labels in the tuple to keep for the
            labeled point (0 to 2 for imr dataset)
    :param l_encoder: the label encoder to encode the label (if any)

    :return: a LabeledPoint
    """

    from pyspark.mllib.classification import LabeledPoint
    label = entry[category]
    if l_encoder:
        label = l_encoder.transform(label)
    features = entry[-1]
    return LabeledPoint(label, features)  # return a new labelPoint

def classify_line(features, model, l_encoder=None):
    """
    Classifies the features based on the given model.
    If a label encoder is specified, it reverses the encoding of the label
    :param features: a vector of features
    :param model: a Classification Model
    :param l_encoder: a LabelEncoder
    :return: a tuple of:    label, [feat1, feat2 ... featN]
    """
    encoded_prediction = model.predict(features)
    prediction = l_encoder.inverse_transform(encoded_prediction) \
        if l_encoder else encoded_prediction
    return prediction, features


def label_encoders_from_json_file(labels_json_file, category=None):
    """
    Loads a mapping of categories->available_labels from a json file.
    If category is specified it returns the LabelEncoder for this category.
    If not, it returns a dict of category->LabelEncoder
    :param labels_json_file:
    :param category:
    :return:
    """
    from json import load
    from sklearn.preprocessing import LabelEncoder
    with open(labels_json_file) as infile:

        all_labels = load(infile)
        label_dict = dict(map(
                lambda (k, v): (int(k), LabelEncoder().fit(v)),
                all_labels.iteritems()
        ))

        return label_dict[category] if category else label_dict


def labels_from_csv_file(csv_file, label_range):
    """
    Parses a csv dataset and keeps a set of all the labels in 'label_range'.
    Preserves the order in which it sees labels - does not contain duplicates.
    :param csv_file:
    :param label_range:
    :return:
    """
    labels = defaultdict(list)
    label_sets = defaultdict(set)
    with open(csv_file) as infile:
        for line in infile:
            line_tokens = line.split(';')
            for i in range(label_range[0], label_range[1]+1):
                label = int(line_tokens[i])
                if label not in label_sets[i]:
                    label_sets[i].add(label)
                    labels[i].append(label)
    # convert to regular dict of lists
    return dict(labels.iteritems())


# =======================  MAIN ========================= #
if __name__ == "__main__":
    from argparse import ArgumentParser
    from json import dump

    cli_parser = ArgumentParser(description='tools for imr datasets')
    cli_parser.add_argument("operation",
                            help="the operation to run: 'train' or 'classify'")
    cli_parser.add_argument("input",
                            help="the input dataset (formatted as a csv file"
                                 "separated with ';' character")
    cli_parser.add_argument("output", help="the output file")
    cli_parser.add_argument("-rs", '--range-start', type=int, default=1,
                            help="the start of the range of labels")
    cli_parser.add_argument("-re", '--range-end', type=int, default=3,
                            help="the end of the range of labels (inclusive)")

    args = cli_parser.parse_args()

    if args.operation == "storelabels":
        from collections import defaultdict
        # get a dict of labels from a csv dataset
        labels_dict = labels_from_csv_file(args.input,
                                           (args.range_start, args.range_end))
        # dump it to the output file
        with open(args.output, 'w+') as outfile:
            dump(labels_dict, outfile)



    else:
        print("I do not know operation:", args.operation)


