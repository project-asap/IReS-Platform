#!/bin/bash



install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y update 1>>/tmp/apt.log 2>>/tmp/apt.log
apt-get -y install maven git vim bash-completion tomcat7 openjdk-7-jdk 1>>/tmp/apt.log 2>>/tmp/apt.log
}



install_packages
