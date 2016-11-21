if [ $@ -z ]; then
	echo wrong input
	exit
fi

# create an ssh tunnel

ssh  -L1234:localhost:3128 imr_master -N &

sleep 1

curl -x localhost:1234 http://imr41.internetmemory.org:9200/europeannews/resource/_search?q=textContent:$@ > $@.result


# assume tunnel is first job
kill %1
