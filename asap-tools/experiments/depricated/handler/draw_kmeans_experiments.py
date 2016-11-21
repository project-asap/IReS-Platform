__author__ = 'cmantas'
from tools import *

def draw_kmeans_forDF(engine, minDF, list_k= [5,10,15,20]):
    for k in list_k:
        draw_single_kmeans(engine, k, minDF, title=engine.title()+" K-means (minDF={})".format(minDF), hide_minDF=True)

def draw_kmeans_forK(engine, k, list_minDF= [10,60,110,160]):
    for minDF in list_minDF:
        draw_single_kmeans(engine, k, minDF, title=engine.title()+" K-means (K={})".format(k))





k_list=  [5,10,15,20];


# print list_k
# multi_graph("mahout_kmeans_text", "documents/1000", "avg(time/1000)", cond_producer("k", [5,10,15,20]), groupBy="documents", title="Mahout K-Means Documents vs Time", where="minDF=10")


draw_kmeans_forDF("mahout", 10)
figure()
draw_kmeans_forDF("spark", 10)
show()

draw_kmeans_forK("mahout", 10)
figure()
draw_kmeans_forK("weka", 10)
show()
exit()

# Spark Kmeans
# multi_graph("spark_kmeans_text", "avg(documents/1000)", "time/1000", cond_producer("minDF=10 and k", k_list), groupBy="documents", title="Spark Documents vs Time")

#
# exit()


# multi_graph("mahout_kmeans_text", "input_size/1048576", "time", ["k=5", "k=15", "k=15", "k=20"], groupBy="documents")
# exit()

# Weka Kmeans
# figure()
# for k in k_list:
#     draw_single_kmeans("weka",  k, 10)


# minDF vs time






## K-means
k=10
figure()
draw_single_kmeans("weka", k, 10,title="K-Means WEKA, Documents vs Time")
draw_single_kmeans("weka", k, 60)
draw_single_kmeans("weka", k, 110,  where_extra=" weka_tfidf.documents<5500")
draw_single_kmeans("weka", k, 160)

show()
exit()