from time import time
from signal import SIGINT, signal
from json import dump
from sys import exc_info
import traceback
import logging as log
from log_lib import get_logger

log = get_logger('Ss_REPORT', level='INFO', show_level=True)


class StreamReporter(object):

    def __init__(self):
        # boolean flag indicating the start of a stream
        self.started = False
        # start time of a batch
        self.start_time = -1
        # the timestamp when the previous rdd was computed
        self.prev_time = None
    
        # list of timestamps of when the rdds were created
        self.rdd_times = []
        # list of processed records on each time (delta)
        self.records = []
    
    
        # output file for the streaming experiment statistics
        self._STATS_FILE = "/tmp/spark_streaming_experiment.json"

        # how many empty rdds to expect before marking a stream stopped
        self._ERCI = 5

    def report_stream_stats(self):

        # remove the  self._ERCI places-before-last in self.rdd_times
        #  since they represent a finished stream
        self.rdd_times = self.rdd_times[: - self._ERCI]
        self.records = self.records[: -self._ERCI]

        # the timestamp when the last RDD was computed is
        #  the end of the d-stream
        end_time = self.rdd_times[-1]

        duration = end_time - self.start_time

        log.info("---==> Experiment END: "+("%.1f" % duration)+"sec, "+
                 str(sum(self.records))+" records")
        log.debug(" end (detected end in: "+ str(time()-self.start_time)+")")

        # shift times in self.rdd_times so that they start from zero
        self.rdd_times = map(lambda t: t-self.start_time, self.rdd_times)

        # output the data in a file
        stats = {"time":duration, "timestamps:": self.rdd_times,
                 "records": self.records, "total_records":sum(self.records)}

        with open(self._STATS_FILE, "w+") as f:
            dump(stats, f, indent=3)

    def handle_each_microbatch_end(self, count):
        try:

            # 1) check if self.started
            if not self.started and count > 0:
                self.started = True
                self.start_time = self.prev_time if self.prev_time else time()
                # add the zero-point in the stats
                self.records.append(0)
                self.rdd_times.append(self.start_time)
                log.info("---==> Experiment START")

            # 2) Update time delta
            now = time()
            time_delta = now - self.prev_time\
                if self.prev_time is not None else None

            # 3) update records, timestamp lists
            if self.started:
                self.records.append(count)
                self.rdd_times.append(now)

            # 4) report deltas
            if self.started:
                log.debug(str(count) + " entries in " + ("%.1f" % time_delta) if
                          time_delta else "None" + " seconds")

            # 5) check if stopped and reset
            if self.started and sum(self.records[-self._ERCI:]) == 0:
                log.debug("++++++______ stopped ______++++++")
                self.report_stream_stats()
                self.started = False
                self.records = []
                self.rdd_times = []

            # last) update prev time with now
            self.prev_time = now

        except :
            print("ERROR: something went wrong with reporting the stats...\n", exc_info()[0])
            traceback.print_exc()


class SparkDstreamReporter(StreamReporter):

    def __init__(self, dstream,  disabled=False):
        super(SparkDstreamReporter, self).__init__()
        self.dstream = dstream
        self.spark_streaming_context = dstream.context()
        self.install_signal_handler()
        # set the handler of foreachRDD
        if not disabled:
            dstream.foreachRDD(lambda arg: self.handle_each_dstream_rdd(arg))

    def handle_each_dstream_rdd(self, an_rdd):
        # print "Hellooo:",len(args)
        # an_rdd = args[0]
        self.handle_each_microbatch_end(an_rdd.count())

    @staticmethod
    def stop_spark_streaming_gracefully(self, spark_streaming_context, *args):
        print("\n====> STOPPING GRACEFULLY Spark Streaming Context, etc")
        spark_streaming_context.stop()
        print("DONE")
        exit()

    def install_signal_handler(self):
        handler = lambda sig,frame: \
            SparkDstreamReporter.stop_spark_streaming_gracefully(
                    self.spark_streaming_context, sig, frame
            )
        signal(SIGINT, handler)

