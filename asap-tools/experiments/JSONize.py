import sqlite3
import json

con = sqlite3.connect("vic_results.db")
data = con.execute("select input_size, documents, execTime\
		    from lda_scala2").fetchall()

serialized = dict()
serialized['benchmarks'] = []

for d in data:
	serialized['benchmarks'].append(\
					{'outputSpacePoints': {'execTime':float(d[2])}, \
					 'inputSpacePoints':{'input_size': float(d[0]), 'documents': float(d[1])}})

with open('execTime.json', 'w+') as out_file:
	json.dump(serialized, out_file)
