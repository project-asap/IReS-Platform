#!/bin/bash

install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y update 1>>/tmp/apt.log 2>>/tmp/apt.log
apt-get -y install bash-completion vim apache2-utils siege 1>>/tmp/apt.log 2>>/tmp/apt.log
}



install_packages
