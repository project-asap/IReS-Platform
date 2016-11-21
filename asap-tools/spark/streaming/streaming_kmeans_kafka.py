from __future__ import print_function
from streaming_reporter_tools import SparkDstreamReporter
from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from pyspark.mllib.clustering import StreamingKMeans
from argparse import ArgumentParser


# like streaming Kmeans, but also reports
class WatchedStreamingKMeans(StreamingKMeans):
    def trainOn(self, dstream):
        """Train the model on the incoming Dstream and then report """
        # validate that the input is of the correct format
        self._validate(dstream)

        # create a reporter
        reporter = SparkDstreamReporter(dstream, disabled=True)

        def update(rdd):
            count = rdd.count()
            # if there are actual ipput data, update the model
            if count > 0:
                # only update for non-empty rdds
                self._model.update(rdd, self._decayFactor, self._timeUnit)

            # after updating the model run the reporter
            reporter.handle_each_microbatch_end(count)

        # for each RDD in the D-stream, update the previous model
        dstream.foreachRDD(update)


def parse_imr_w2v_vector(v_str):
    """
    creates a spark DenseVectorvectors from string lines
    :param v_str:
    :return:
    """
    from pyspark.mllib.linalg import DenseVector
    num_vec = map(float, v_str.split(';')[1:])  # TODO:? discard labels
    return DenseVector(num_vec)

parser = ArgumentParser("producer for kafka that reads first L lines from file")
parser.add_argument('-i', "--interval",
                    help="the interval between each batch (secs)",
                    type=int, default=3)
parser.add_argument('-k', "--clusters", help="the number of clusters",
                    type=int, required=True)
args = parser.parse_args()



# SparkContext and StreamingSpark Context
sc = SparkContext(appName="Pyspark-Streaming-Kmeans")
ssc = StreamingContext(sc, args.interval)



# a D-Stream of input lines from Kafka
lines = KafkaUtils.createStream(ssc,
                                "master:2181",      # broker_host:port
                                "consumer-group",
                                {"test": 1}         # topic to read from
                                )\
    .map(lambda t: t[1])  # use the second item and discard the first

# a D-Stream of (Sparse) Vectors
vectors = lines.map(parse_imr_w2v_vector)

print("----K="+str(args.clusters))

# the k-means model
model = WatchedStreamingKMeans(k=args.clusters, decayFactor=1.0)\
    .setRandomCenters(202, 1.0, 0)


model.trainOn(vectors)


# start the streaming context
ssc.start()
print("========> Started Streaming Context <==========")

# await termination of streaming context
ssc.awaitTermination()
print("========> Stopped Streaming Context <==========")

