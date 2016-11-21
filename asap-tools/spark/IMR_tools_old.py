__author__ = 'cmantas'

from sklearn.preprocessing import LabelEncoder
from pyspark.mllib.classification import LabeledPoint
import joblib
from sys import maxint


input_fname = "/home/cmantas/Data/Result_W2V_IMR_New.csv"
test1_fname = "/home/cmantas/Data/Result_W2V_IMR_test1.csv"

wanted_lines = 100


def get_labels_from_imr_dataset(line):
    """
    Parses a line of the IMR dataset and only keeps the labels
    :param line:
    :return: a tuple of the 2 labels
    """
    sl = line.split(";")
    if not sl[1].isdigit():
        return
    label1 = int(sl[1])
    label2 = int(sl[2])
    return (label1, label2)


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
    Parses the line using the parser function lambda, and creates a LabeledPoing with
    the 'wanted' category as label
    :param line: the line to parse
    :param parser_function: the lambda function that creates the tuples
    :param line: the string line to parse
    """
    labels = labels_and_features[0]
    features = labels_and_features[1]

    return LabeledPoint(labels[wanted_category], features)

def get_labels_from_csv_dataset(raw_data_rrd):
    """
    Given an imr csv file, returns the set of unique cat1 and cat2 labels in that file
    :param fname: The path to the csv file
    :return:
    """
    label_tuples_rrd = raw_data_rrd.map(get_labels_from_imr_dataset).filter(lambda line: line is not None)
    l1_rdd = label_tuples_rrd.map(lambda (l1,l2): l1)
    l2_rdd = label_tuples_rrd.map(lambda (l1,l2): l2)
    labels_1 = l1_rdd.distinct().collect()
    labels_2 = l2_rdd.distinct().collect()
    return labels_1, labels_2

def create_label_encoders(input_csv_file):
    labels_1, labels_2 =  get_labels_from_csv_dataset(input_csv_file)
    L1_encoder = LabelEncoder();L1_encoder.fit(labels_1)
    L2_encoder = LabelEncoder();L2_encoder.fit(labels_2)
    return L1_encoder, L2_encoder

def store_label_encoders(enc1, enc2, le_path):
    joblib.dump( (enc1, enc2), le_path)

def load_label_encoders(le_path):
    (l1e, l2e) = joblib.load(le_path)
    return l1e, l2e


def read_file_and_get_labels(imr_fname, wanted_lines=maxint):
    labels1 = []
    labels2 = []
    line_count=0
    with open(test1_fname) as f:
        for line in f:
            # limit the read lines
            line_count +=1
            if line_count>=wanted_lines: break

            labels = get_labels_from_imr_dataset(line)
            if labels is None: continue
            labels1.append(labels[0])
            labels2.append(labels[1])

    return labels1, labels2




def do_encoding(input_fname, output_fname, line_encoding_function,  wanted_lines=maxint):
    fout = open(input_fname, "w+")

    line_count=0
    with open(input_fname) as f:
        for line in f:
            # limit the read lines
            line_count +=1
            if line_count>=wanted_lines: break
            # pass the line to the encoding function
            point = line_encoding_function(line)
            if point is None: continue

            # construct the output string and write it
            string_labels = str(point[0][0])+";"+str(point[0] [0]) +";"
            string_features = map(lambda f: str(f),point[1])
            re_mapped_line =  "0.0;"+string_labels+";".join(string_features)
            fout.write(re_mapped_line+"\n")
    fout.close()





print "Phase1:",

labels1, labels2 = read_file_and_get_labels(input_fname, wanted_lines)


# create the label encoders (mapping from big_integer_labels -> consecutive_small_integer_label
L1_encoder = LabelEncoder();L1_encoder.fit(labels1)
L2_encoder = LabelEncoder();L2_encoder.fit(labels2)

print L1_encoder.get_params()


print "DONE \nLabel counts of",wanted_lines,"lines are: \n\t" , len(L1_encoder.classes_), len(L2_encoder.classes_)
print "\tmax class label values: ", max(L1_encoder.classes_), max(L2_encoder.classes_)

# with open("labels_1.csv","w+") as l1f:
#     # convert to list of strings
#     label_strings = map(str, L1_encoder.classes_.tolist())
#     l1f.write(",".join(label_strings))
#
# with open("labels_2.csv","w+") as l1f:
#     label_strings = map(str, L2_encoder.classes_.tolist())
#     l1f.write(",".join(label_strings))



# a function that encodes labels based on L1, L2 encoders
encoding_closure = lambda l: parse_imr_line_encoding_labels(L1_encoder, L2_encoder, l)

do_encoding(input_fname, test1_fname, encoding_closure)

print " OK"



