#!/bin/bash

install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y install ganglia-monitor 1>/dev/null 2>/dev/null
apt-get -y install gmetad 1>/dev/null 2>/dev/null
apt-get -y install ganglia-webfrontend 1>/dev/null 2>/dev/null
}

setup_web(){
ln -s /etc/ganglia-webfrontend/apache.conf /etc/apache2/sites-available/ganglia
a2ensite ganglia
a2enmod rewrite
sed -i -e 's/readonly/disabled/g' /usr/share/ganglia-webfrontend/conf_default.php
chmod guo+w /var/lib/ganglia/conf/*

service apache2 restart
}

configure_gmetad(){
# data_source
sed -i -e 's/^data_source.*/data_source "master" 10 master1:8649/g' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "slaves" 10 slave1:8649' /etc/ganglia/gmetad.conf
# gridname
sed -i -e 's/^# gridname "MyGrid"/gridname "Hadoop cluster"/g' /etc/ganglia/gmetad.conf

# all hosts trusted
sed -i -e 's/^# all_trusted on/all_trusted on/g' /etc/ganglia/gmetad.conf

service gmetad restart
}

configure_gmond(){
# udp_send_channel remove mcat_join and add host
MASTER_IP=$(cat /etc/hosts | grep master | awk '{print $1}')
sed -i -e "/^udp_send_channel/ a \  host=$MASTER_IP" /etc/ganglia/gmond.conf
sed -i -e "s/  mcast_join/# mcast_join/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf

sed -i -e '/^cluster {/{n;d}' /etc/ganglia/gmond.conf
sed -i -e "/^cluster {/ a \  name=\"master\"" /etc/ganglia/gmond.conf

service ganglia-monitor restart
}

restart_all() {
service gmetad restart
service ganglia-monitor restart
NUMBER_OF_SLAVES=$(cat /etc/hosts | grep slave | wc -l)
for i in $(seq 1 $NUMBER_OF_SLAVES); do
  ssh slave$i "service ganglia-monitor restart"
done
}

install_packages
configure_gmond
configure_gmetad
setup_web
restart_all

echo "Ganglia installed"