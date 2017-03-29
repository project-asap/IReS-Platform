SPARK_MASTER=$1
#SPARK_HOME=/home/forth/asap4all/spark01
SPARK_HOME=$2
DRIVER_MEMORY=$3
EXECUTOR_MEMORY=$4
export PYTHONPATH='.':$PYTHONPATH
echo $@
$SPARK_HOME/bin/spark-submit --py-files cdr.py  --master $SPARK_MASTER --driver-memory $DRIVER_MEMORY --executor-memory $EXECUTOR_MEMORY presence.py  $5 $6 $7 "$8" $9 ${10}
