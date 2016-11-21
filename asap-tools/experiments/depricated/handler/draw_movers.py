__author__ = 'cmantas'
from tools import *

def draw_single_move(frome, toe, minDf, **kwargs):
    if frome == "weka":
        table='arff2'+toe
    elif toe == "weka":
        table=frome+'2arff'
    else:
        table=frome+"2"+toe

    tfidf_table= frome+"_tfidf"
    query = join_query({'table':table, 'tfidf_table':tfidf_table, 'minDF':minDf})
    plot_from_query(query, **kwargs)


def draw_many_moves(frome, toe, minDf_list=[10, 60, 110, 160]):
    figure()
    kwargs={}
    for minDF in minDf_list:
        kwargs['label']="minDF="+str(minDF)
        kwargs['title'] ="Move "+ frome.title()+" to "+toe.title()
        kwargs['ylabel']= "time (sec)"
        kwargs['xlabel'] = 'documents/1000'
        draw_single_move(frome, toe, minDF, **kwargs)


draw_many_moves("mahout","weka")
draw_many_moves("mahout","spark")
show()

exit()

def docs_vs_time(mover, tfidf, list, **kwargs):
    join_multi(mover, tfidf,"documents/1000", "time/1000", list, **kwargs)

def size_vs_time(mover, tfidf, list, **kwargs):
    join_multi(mover, tfidf,"input_size/1048576", "time/1000", list, **kwargs)



# docs_vs_time("mahout2spark", "mahout_tfidf", cond_producer("minDF", [10, 60, 110, 160]), title="Move Mahout 2 Spark", xlabel="documents/1000", ylabel="time (sec)")
# size_vs_time("mahout2spark", "mahout_tfidf", cond_producer("minDF", [10, 60, 110, 160]), title="Move Mahout 2 Spark", xlabel="size (MB)", ylabel="time (sec)")
#
# size_vs_time("mahout2arff", "mahout_tfidf", cond_producer("minDF", [10, 60, 110, 160]), title="Move Mahout to arff", xlabel="size (MB)", ylabel="time (sec)")
# size_vs_time("spark2mahout", "spark_tfidf", cond_producer("minDF", [10, 60, 110, 160]), title="Move Spark to Mahout", xlabel="size (MB)", ylabel="time (sec)")
size_vs_time("spark2arff", "spark_tfidf", cond_producer("minDF", [10, 60, 110, 160]), title="Move Spark to arff", xlabel="size (MB)", ylabel="time (sec)")


# # # multi_graph_query(query, cond_producer("minDF", [10, 60, 110, 160]), )
show()


# figure()
# rx, ry = query2lists("select input_size/1048576, time/1000 from mahout2spark order by input_size ")
# myplot(rx,ry)
# show()

exit()
