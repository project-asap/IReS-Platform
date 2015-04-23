#!/bin/bash

install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y update 1>>/tmp/apt.log 2>>/tmp/apt.log
apt-get -y install ganglia-monitor gmetad ganglia-webfrontend 1>>/tmp/apt.log 2>>/tmp/apt.log
}

setup_web(){
ln -s /etc/ganglia-webfrontend/apache.conf /etc/apache2/sites-available/ganglia
a2ensite ganglia

service apache2 restart
}

configure_gmetad(){
# data_source
sed -i -e 's/^data_source.*/data_source "master" 10 master1:8649/g' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "slaves" 10 slave1:8649' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "tomcat" 10 tomcat1:8649' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "client" 10 client1:8649' /etc/ganglia/gmetad.conf
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
#NUMBER_OF_SLAVES=$(cat /etc/hosts | grep slave | wc -l)
#for i in $(seq 1 $NUMBER_OF_SLAVES); do
#  ssh slave$i "service ganglia-monitor restart"
#done
}

install_packages
configure_gmond
configure_gmetad
setup_web
restart_all

echo "Ganglia installed"