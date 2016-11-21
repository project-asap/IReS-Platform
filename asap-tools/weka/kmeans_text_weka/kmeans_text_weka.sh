source $(dirname $0)/common.sh
input=$1
iterations=$3
clusters=$2
output=$4

echo "STEP 3/3: K-Means"

java -Xmx15g -cp ${WEKA} weka.clusterers.SimpleKMeans \
	     -N ${clusters} \
	     -I ${iterations}  \
	     -A "weka.core.EuclideanDistance -R first-last" \
	     -t $input \
	     > $output
head -n 30 clusters.txt
echo DONE KMEANS
rm clusters.txt	
