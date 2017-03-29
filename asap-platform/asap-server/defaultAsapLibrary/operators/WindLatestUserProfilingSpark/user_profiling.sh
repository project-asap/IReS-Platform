# TODO set SPARK_HOME if not exists
SPARK_MASTER=$1
SPARK_HOME=$2
EXECUTOR_MEMORY=$3
export PYTHONPATH='.':$PYTHONPATH
$SPARK_HOME/bin/spark-submit --py-files cdr.py --master $SPARK_MASTER --executor-memory $EXECUTOR_MEMORY user_profiling.py $4 $5 $6 "$7" $8 $9 ${10}
