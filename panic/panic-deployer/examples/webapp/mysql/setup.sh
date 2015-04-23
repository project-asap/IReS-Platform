#!/bin/bash

export DEBIAN_FRONTEND=noninteractive
export GITHUB_URL="https://github.com/giagiannis/cloud-benchmark-apps.git"

apt-get -y update >/dev/null
apt-get -y install mysql-server git >/dev/null

sed -i -e "s/bind-address/# bind-address/g" /etc/mysql/my.cnf >/dev/null
service mysql restart >/dev/null

git clone $GITHUB_URL apps >/dev/null
mysql -u root < apps/webapp/database.sql

# database is ready to accept connections