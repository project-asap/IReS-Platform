#!/usr/bin/env python
__author__ = 'cmantas'
from tools import *



mindDF_list = ["minDF=10", "minDF=60","minDF=110","minDF=160"]

# Docs vs Terms
# multi_graph("mahout_tfidf", "documents/1000", "dimensions/1000", mindDF_list, title="Mahout Documents vs Terms", where="documents<60400")
# multi_graph("weka_tfidf", "avg(documents/1000)", "dimensions/1000", mindDF_list, title="weka Documents vs Terms", groupBy="documents")
#

# Stemming vs not: Docs vs terms Mahout and Weka
# plot_from_query("select documents/1000,dimensions/1000 from mahout_tfidf where documents<60400 and minDF=10",
#                 label="WEKA (stemmed)", title="Docs vs Terms: Effect of stemming", ylabel="# terms/1000", xlabel="documents/1000")
# plot_from_query("select avg(documents/1000),dimensions/1000 from weka_tfidf where minDF=10 group by documents", label="Mahout (not stemmed)")
# show()

# # exit()
# #
# # multi_graph("mahout_tfidf", "documents/1000", "time/1000", mindDF_list, ylabel='time (sec)', title="Mahout Documents vs Time")
# multi_graph("weka_tfidf", "avg(documents/1000)", "time/1000", mindDF_list, ylabel='time (sec)', groupBy="documents",  title="Weka Documents vs Time")
# # exit()
#
#
#
#
# # Mahout Kmeans
list_k = cond_producer("minDF=10 and k", [5,10,15,20])

#mahout tfidf impact of minDF on output size
multi_graph("mahout_tfidf", "documents/1000", "output_size/1048576", mindDF_list,  title="Mahout TF/IDF Documents vs Time", ylabel="output size (MB)")

multi_graph("mahout_tfidf", "input_size/1048576", "output_size/1048576", mindDF_list,  title="Mahout TF/IDF Documents vs Time", ylabel="output size (MB)", xlabel="output size (MB)")
show()

# multi_graph("weka_tfidf", "input_size/1048576", "output_size/1048576", mindDF_list,  groupBy="documents", title="Weka Input vs Output size")
# multi_graph("spark_tfidf", "input_size/1048576", "output_size/1048576", mindDF_list,  groupBy="documents",title="Spark Input vs Output size")
show()
# exit()




# spark tfidf minDF vs output size
# multi_graph("spark_tfidf", "documents/1000", "time/1000", ["minDF=10 and documents<120000", "minDF=60", "minDF=110", "minDF=160"],  title="Documents vs Time", ylabel="time (sec)", groupBy="documents")

plot_from_query("select documents/1000,time/1000 from spark_tfidf where minDF=10 and documents<120000 group by documents", label="minDF=10", title="Spark TF/IDF", ylabel="time (sec)", xlabel="#docs/1000")
plot_from_query("select documents/1000,time/1000 from spark_tfidf where minDF=60 group by documents", label="minDF=60")
plot_from_query("select documents/1000,time/1000 from spark_tfidf where minDF=110 group by documents", label="minDF=110")
plot_from_query("select documents/1000,time/1000 from spark_tfidf where minDF=160 group by documents", label="minDF=160")
show()

# exit()


# #spark kmeans impact of minDF on time
# multi_graph("spark_kmeans_text", "documents/1000", "time/1000", ["minDF=10 and k=15", "minDF=60 and k=15", "minDF=110 and k=15", "minDF=160 and k=15"],  title="Documents vs Time", ylabel="output_size (MB)")
# show()
# exit()

# spark kmeans multi K
multi_graph("spark_kmeans_text", "documents/1000", "time/1000", ["minDF=10 and k=15", "minDF=60 and k=15", "minDF=110 and k=15", "minDF=160 and k=15"],  title="Documents vs Time", ylabel="output_size (MB)")
# show()

# #spark tfidf impact of minDF on output size
# multi_graph("spark_tfidf", "documents/1000", "output_size/1048576", ["minDF=10 ", "minDF=60 ", "minDF=110", "minDF=160"],  title="Documents vs Time", ylabel="output size (MB)")
# show()
# exit()



#mahout vs spark tfidf
figure()
plot_from_query("select documents/1000 ,avg(time/1000) from weka_tfidf  where minDF=10 group by documents ", label='weka')
plot_from_query("select documents/1000 ,avg(time/1000) from mahout_tfidf where minDF=10 group by documents ", title="TF/IDF: WEKA, Mahout, Spark (minDF=10)", ylabel='time (sec)', xlabel='documents/1000', label="mahout")
plot_from_query("select documents/1000 ,avg(time/1000) from spark_tfidf  where minDF=10 group by documents ", label='spark')


show()
exit()


## OUTPUT


#
#
# figure()
# minDF=10
# query = "select documents, avg(dimensions) from weka_tfidf where mindf=%d and documents<=11000 group by documents;"
# docs,terms = query2lists(query%minDF)
# myplot(docs,terms, label="minDF=%d"%minDF, title="Weka Documents vs Terms", xlabel="#docs", ylabel="#terms")
# minDF=110
# docs,terms = query2lists(query%minDF)
# myplot(docs,terms, label="minDF=%d"%minDF)
# minDF=210
# docs,terms = query2lists(query%minDF)
# myplot(docs,terms, label="minDF=%d"%minDF)
# minDF=310
# docs,terms = query2lists(query%minDF)
# myplot(docs,terms, label="minDF=%d"%minDF)
# show()




