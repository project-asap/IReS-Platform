all:
	# Building asap-tools
	@mvn clean compile assembly:single>/dev/null ;
	# Moving jars / Installing
	mkdir -p $(HOME)/bin/lib &>/dev/null;  mv target/asapTools*.jar ~/bin/lib/asapTools.jar
	
