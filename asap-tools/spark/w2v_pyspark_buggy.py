from pyspark import SparkContext
from pyspark.mllib.feature import Word2Vec, Word2VecModel

from shutil import rmtree
from os.path import isdir


in_file = "/home/cmantas/Data/imr_training_suffled_small.csv"
model_path = "/home/cmantas/Data/w2v_pyspark_model"

DELIMS = r'[- .,;\'"()]'  # a regex with possible delimiters
VEC_SIZE = 200


def parse_raw_line(line):
    elements = line.split(";")
    # first 4 elements are id, cat1, cat2, cat3
    id_and_labels = map(int, elements[:3])

    # the rest are considered a string
    text = " ".join(elements[4:])
    return id_and_labels + [text]


def tokenize_line(line):
    text = line[-1]  # the last element of line is the text
    from re import split
    return filter(lambda w: len(w)>1, split(DELIMS, text.lower()))


sc = SparkContext(appName="Train Logistic Regression (IMR)")

parsed_data = sc.textFile(in_file).map(parse_raw_line)

vectors_of_words = parsed_data.map(tokenize_line)

word2vec = Word2Vec().setVectorSize(VEC_SIZE)
model = word2vec.fit(vectors_of_words)

try:
    rmtree(model_path)
except:
    pass

model.save(sc, model_path)

sameModel = Word2VecModel.load(sc, model_path)

print len(sameModel.getVectors())

