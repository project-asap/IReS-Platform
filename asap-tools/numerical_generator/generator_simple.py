#!/usr/bin/python

__author__ = 'cmantas'

import numpy as np
from sys import maxint
from os import makedirs
import argparse
from sys import stderr

parser = argparse.ArgumentParser(description='generates a .csv file with uniformly random integer data')
parser.add_argument("-n","--number", help="how many points to plot",type=int, required=True)
parser.add_argument("--dimensions", '-d', type=int, help="the dimensions of the resulting vectors", required=True)
parser.add_argument("--outdir", '-o', help="the output directory", )
args = parser.parse_args()


count = args.number
dimension = args.dimensions
if args.outdir is None:
    out_dir = "."
else:
    out_dir = args.outdir

fname = out_dir+"/"+ "{0:010d}_points_{1:05d}_dimensions".format(count, dimension)+".csv"
stderr.write("Writing {0} {1}-dimensional points to file: {2}\n".format(count, dimension, fname))

try: makedirs(out_dir)
except: pass

f = open(fname, "w")

for i in range(count):
    list = np.random.randint(-maxint, maxint, size=dimension).tolist()
    f.write(",".join("{0}".format(n) for n in list) + "\n")

f.close()

print fname
