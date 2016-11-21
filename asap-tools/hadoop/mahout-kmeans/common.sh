chunk=30
TOOLS_JAR="/home/$USER/bin/lib/asapTools.jar"

#tfidf_env="-Dmapred.child.ulimit=15728640 -Dmapred.child.java.opts=-Xmx5g \
#	           -Dmapred.map.tasks=10 "

check (){
        e=$( cat $1 | grep -E "Exception|ERROR: " )
        t=$( echo $e | wc -c)
        if [ "$e" != "" ]; then
                echo $e
                exit -1
        fi
}

