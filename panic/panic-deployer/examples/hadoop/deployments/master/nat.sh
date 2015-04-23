#!/bin/bash

# the router's private IP
#GATEWAY_PRIVATE_IP=$(cat /etc/hosts | grep master | awk '{print $1}')
# the router's public ip
#GATEWAY_PUBLIC_IP=$(ifconfig  | grep 83.212. | tr ':' "\t"| awk '{print $3}')

echo "Enabling ipv4 forwarding (cleaning old rules)"
# flushing old rules -- USE WITH CARE
iptables --flush
iptables --table nat --flush
# MASQUERADE each request form the inside to the outer world
iptables -t nat -A POSTROUTING -j MASQUERADE
# enable IPv4 packet forwarding in the kernel
echo 1 > /proc/sys/net/ipv4/ip_forward

echo "Master is not operating as router"


#echo "Applying port-forwarding rules"
# Port forwarding
#iptables -t nat -A PREROUTING -p tcp -d $GATEWAY_PUBLIC_IP --dport 50070 -j DNAT --to 192.168.0.3:50070
#iptables -t nat -A PREROUTING -p tcp -d $GATEWAY_PUBLIC_IP --dport 50070 -j DNAT --to 192.168.0.7:50070
#iptables -t nat -A PREROUTING -p tcp -d $GATEWAY_PUBLIC_IP --dport 50030 -j DNAT --to 192.168.0.7:50030
#iptables -t nat -A PREROUTING -p tcp -d $GATEWAY_PUBLIC_IP --dport 60010 -j DNAT --to 192.168.0.7:60010




# Update the routing tables of the rest lads inside the LAN
#for HOST in server{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15}; do
#        ssh $HOST "route add default gw $GATEWAY_PRIVATE_IP && route del default gw 192.168.0.1 " 2>/dev/null;
#        echo "Updated routing tables for $HOST";
#done
