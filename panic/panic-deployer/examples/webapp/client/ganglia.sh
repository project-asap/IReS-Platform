#!/bin/bash

install_packages(){
export DEBIAN_FRONTEND=noninteractive
apt-get -y install ganglia-monitor gmetad ganglia-webfrontend 1>/dev/null 2>/dev/null
}

setup_web(){
ln -s /etc/ganglia-webfrontend/apache.conf /etc/apache2/sites-available/ganglia
a2ensite ganglia

service apache2 restart
}

configure_gmetad(){
# data_source
sed -i -e 's/^data_source.*/data_source "apache" 10 apache1:8649/g' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "mysql" 10 mysql1:8649' /etc/ganglia/gmetad.conf

sed -i -e '/^data_source / a \data_source "client" 10 client1:8649' /etc/ganglia/gmetad.conf
# gridname
sed -i -e 's/^# gridname "MyGrid"/gridname "Web Application"/g' /etc/ganglia/gmetad.conf

# all hosts trusted
sed -i -e 's/^# all_trusted on/all_trusted on/g' /etc/ganglia/gmetad.conf

service gmetad restart
}

configure_gmond(){
# udp_send_channel remove mcat_join and add host
LOCALHOST=$(cat /etc/hosts | grep client1 | awk '{print $1}')
sed -i -e "/^udp_send_channel/ a \  host=$LOCALHOST" /etc/ganglia/gmond.conf
sed -i -e "s/  mcast_join/# mcast_join/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf
sed -i -e "s/  bind/# bind/" /etc/ganglia/gmond.conf

sed -i -e '/^cluster {/{n;d}' /etc/ganglia/gmond.conf
sed -i -e "/^cluster {/ a \  name=\"client\"" /etc/ganglia/gmond.conf

service ganglia-monitor restart
}

install_packages
configure_gmond
configure_gmetad
setup_web


echo "Ganglia installed"
