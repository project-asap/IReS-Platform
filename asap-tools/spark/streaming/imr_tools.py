from pyspark.mllib.regression import LabeledPoint

from sklearn.preprocessing import LabelEncoder
from sklearn.externals import joblib # used to store and load models to disk


def load_label_encoders(le_path):
    (l1e, l2e) = joblib.load(le_path)
    return l1e, l2e

def parse_imr_line(line):
    """
    Parses a line of the IMR csv dataset to tupples
    :param line:
    :return: ( (label1 (int), label2 (int)), features(list of float) )
    """
    sl = line.split(";")
    if not sl[1].isdigit():
        return

    label1 = int(sl[1])
    label2 = int(sl[2])
    features = map(float, sl[3:])
    return ((label1, label2), features)

def parse_imr_line_encoding_labels(L1_encoder, L2_encoder, line):
    """
    Parses a line of the IMR csv dataset to to encoded tupples (with consecutive class IDs)
    :param L1_encoder: the LabelEncoder for the first label
    :param L2_encoder:  the LabelEncoder for the second label
    :param line:
    :return: ( (label1 (int), label2 (int)), features(list of float) )
    """
    rv = parse_imr_line(line)
    if rv is None : return

    (label1, label2), features = rv[0], rv[1]
    l1, l2 = L1_encoder.transform(label1), L2_encoder.transform(label2)
    return ( (l1, l2) , features)

def create_labeled_point( labels_and_features, wanted_category):
    """
    gets the labels and features and creates a Labeled Point with the wanted_category
    :param labels_and_features: a tupple of ((cat_1,cat_2),(feat1,feat2....))
    :param wanted_category: 0 for the first category, 1 for the second    
    """
    labels = labels_and_features[0]
    features = labels_and_features[1]
    
    return LabeledPoint(labels[wanted_category], features)

