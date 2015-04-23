#!/bin/bash

# This scripts install all the dependencies needed by the application


apt-get -y update 1>>/tmp/apt.log 2>>/tmp/apt.log

apt-get -y install vim bash-completion openjdk-7-jre-headless libsnappy1 libsnappy-java 1>>/tmp/apt.log 2>>/tmp/apt.log

echo "Dependencies installed"

