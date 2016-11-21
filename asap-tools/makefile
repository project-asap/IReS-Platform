DIRS = hadoop/asap-tools weka/kmeans_weka


home:=$(shell pwd)

default: build install_scripts 

build:
	# Building things
	@for d in $(DIRS); do (cd $$d; $(MAKE) ); done

install_scripts:
	@echo Setting ASAP_HOME of .bashrc  in '$(home)'
	@# delete previous entry and put the new one
	@sed -i "/export ASAP_HOME=.*/d" ~/.bashrc
	@echo "export ASAP_HOME=$(home)" >> ~/.bashrc
	@sed -i "/export ASAP_HOME=.*/d" ~/.zshrc
	@echo "export ASAP_HOME=$(home)" >> ~/.zshrc
