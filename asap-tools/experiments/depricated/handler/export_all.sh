rm ../*.csv
#!/bin/bash
sqlite3 ../results.db <<!
.headers on
.mode csv
.output weka_kmeans_synth.csv
select * from weka_kmeans_synth ;
.output weka_tfidf.csv
select * from weka_tfidf;
.output weka_kmeans_text.csv
select * from weka_kmeans_text;
.output mahout_kmeans_synth.csv
select * from mahout_kmeans_synth ;
.output mahout_tfidf.csv
select * from mahout_tfidf;
.output mahout_kmeans_text.csv
select * from mahout_kmeans_text;
.output spark_kmeans_text.csv
select * from spark_kmeans_text;
!
