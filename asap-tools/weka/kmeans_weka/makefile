all:
	# Building K-Means weka jar
	@mvn clean compile assembly:single >/dev/null;
	# Moving jars / Installing
	@mkdir -p ~/bin/lib &>/dev/null; mv target/kmeans_weka-1-jar-with-dependencies.jar ~/bin/lib/kmeans_weka.jar
