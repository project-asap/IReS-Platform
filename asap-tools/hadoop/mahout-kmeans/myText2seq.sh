source $(dirname $0)/common.sh
TOOLS_JAR=/home/$USER/bin/lib/asapTools.jar

input=$1
output=$2
count=$3

hadoop jar ${TOOLS_JAR} loadDir $input $output $count $chunk
