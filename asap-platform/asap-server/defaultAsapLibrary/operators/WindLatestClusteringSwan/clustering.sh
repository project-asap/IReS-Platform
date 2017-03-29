export PYTHONPATH='.':$PYTHONPATH
export LD_LIBRARY_PATH=/home/asap/asap/qub/gcc-5/gcc-5.4.0-install/lib64:$LD_LIBRARY_PATH
# export PATH=$PATH:/home/forth/asap4all/qub/bin
echo $@
source /home/asap/virtualenvs/local/bin/activate
python clustering_swan.py ${@:1}
# Just for testing the inputs/outputs to/from the operator:
# hdfs dfs -copyFromLocal centroids/forth/2016_9 test_2016_9
