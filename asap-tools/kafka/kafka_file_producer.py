#!/usr/bin/env python
from os import listdir
from os.path import isfile, isdir
from collections import Iterable
from kafka import SimpleProducer, KafkaClient
from argparse import ArgumentParser


def read_all(files, max_lines=None):
    if not isinstance(files, Iterable):files = [files]

    read_lines =0
    for f in files:
        for line in  open(f, 'r') :
            # limit the output lines
            if max_lines and read_lines >= max_lines:
                return
            yield line
            read_lines +=1


def read_all_from_file_or_dict(path, maxLines=None):
    if isdir(path):
        # construct the list of files to read
        entries = map(lambda e: path+"/"+e, listdir(path))
        files = sorted(filter(isfile, entries))
    elif isfile(path):
        files = [path]
    else:
        raise Exception("The given path ({}) is neither a dir or a file".format(path))
    return read_all(files, maxLines)

def get_kafka_producer(broker, async=True):
    import logging
    logging.basicConfig(
    format='%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s:%(process)d:%(message)s',
    level=logging.ERROR
    )
    kafka = KafkaClient(broker)
    # To send messages asynchronously
    producer = SimpleProducer(kafka, async=async)
    return producer


parser = ArgumentParser("producer for kafka that reads first L lines from file")
parser.add_argument('-f', "--file", help="the input file to read lines from", required=True)
parser.add_argument('-l', "--lines", help="the number of lines to read from input file", type=int , required=True)
parser.add_argument('-b', '--broker', default="localhost:9092")
parser.add_argument('-t', "--topic", help="asynchronous producer", default="test")
parser.add_argument('-a', "--async", help="asynchronous producer", action='store_true')
args = parser.parse_args()


filepath = args.file
topic = args.topic


producer = get_kafka_producer(args.broker, args.async)
# method that sends messages to given topic
send_message = lambda msg: producer.send_messages(topic, msg)


read_lines = 0
read_chars = 0

print "starting"
for l in read_all_from_file_or_dict(filepath, args.lines):
        read_lines +=1
        read_chars += len(l)
        responses = send_message(l)

if read_lines < args.lines:
    print "Not enough lines in file"

print "stopping"
producer.stop()
print "stopped"
print "Read", read_lines, "lines"
print "Read", read_chars, "chars"


