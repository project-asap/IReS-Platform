#!/bin/bash

install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y update 1>>/tmp/apt.log 2>>/tmp/apt.log
apt-get -y install ganglia-monitor 1>>/tmp/apt.log 2>>/tmp/apt.log
}


configure_gmond(){
# udp_send_channel remove mcat_join and add host
CLIENT=$(cat /etc/hosts | grep "client1$" | awk '{print $1}')
sed -i -e "/^udp_send_channel/ a \  host=$CLIENT" /etc/ganglia/gmond.conf
sed -i -e "s/  mcast_join/# mcast_join/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf

sed -i -e '/^cluster {/{n;d}' /etc/ganglia/gmond.conf
sed -i -e "/^cluster {/ a \  name=\"client\"" /etc/ganglia/gmond.conf

service ganglia-monitor restart
}

install_packages
configure_gmond

echo "Ganglia installed"
