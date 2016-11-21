__author__ = 'cmantas'
from tools import *


# Kmeans mahout vs spark

m_q = """select mahout_kmeans_text.documents/1000, mahout_kmeans_text.time/1000
from mahout_tfidf inner join mahout_kmeans_text
ON
	mahout_tfidf.documents=mahout_kmeans_text.documents AND
	mahout_tfidf.dimensions=mahout_kmeans_text.dimensions
where minDF=10 and k={};"""

# plot_from_query(m_q.format(20), label="Mahout, k=20", title="K-Means, Mahout vs Spark", xlabel="#docs/1000", ylabel="#terms")
# plot_from_query("select documents/1000, time/1000 from spark_kmeans_text WHERE k=20 and minDF=10", label="Spark, k=20")


## K-means
# k=10; minDF=10
# figure()
# draw_single_kmeans("weka", k, minDF,title="K-Means: WEKA, Mahout, Spark")
# draw_single_kmeans("mahout", k, minDF)
# draw_single_kmeans("spark", k, minDF, where_extra="spark_kmeans_text.documents<130000")

# show()
# exit()

# tfidf
figure()
plot_from_query("select documents/1000, avg(time/1000) from spark_tfidf where minDF=10 and documents<130000 group by documents",
				label="Spark TF/IDF",  xlabel="#docs/1000", ylabel="time (sec)", title="TF/IDF Performance")
plot_from_query("select documents/1000, time/1000 from mahout_tfidf WHERE minDF=10", label="Mahout, minDF=10")
plot_from_query("select documents/1000, time/1000 from weka_tfidf WHERE minDF=10", label="Mahout, minDF=10")

figure()
plot_from_query("select documents/1000, dimensions/1000 from weka_tfidf where minDF=10", title="doc freq", label="weka")
plot_from_query("select documents/1000, dimensions/1000 from mahout_tfidf where minDF=10", label="mahout")

show()

