#!/bin/bash

export DEBIAN_FRONTEND=noninteractive
export GITHUB_URL="https://github.com/giagiannis/cloud-benchmark-apps.git"

apt-get -y update >/dev/null
apt-get -y install apache2 mysql-client libapache2-mod-php5 php5-mysql git >/dev/null

service apache2 restart >/dev/null


git clone $GITHUB_URL apps > /dev/null

rm -f /var/www/*	# cleaning the www dir
cp apps/webapp/*.php /var/www/	# copy everythinh to www dir