#sbt package

spark-submit \
--driver-memory=2G \
--executor-memory=4G \
--class sparkops.sparkops_2.10.Word2Vec \
target/sparkops_2.10-1.0.jar \
"../Datasets/20newsSmall/*/*" 5
