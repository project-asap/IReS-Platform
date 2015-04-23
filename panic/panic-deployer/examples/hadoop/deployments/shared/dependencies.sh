#!/bin/bash

# This scripts install all the dependencies needed by the application


apt-get -y update 1>/dev/null 2>/dev/null

apt-get -y install vim bash-completion openjdk-7-jre-headless libsnappy1 libsnappy-java time curl 1>/dev/null 2>/dev/null

echo "Dependencies installed"

