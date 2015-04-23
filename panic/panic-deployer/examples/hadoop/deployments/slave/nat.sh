#!/bin/bash

ENDPOINT_INTERFACE=$(cat /etc/hosts | grep master | awk '{print $1}')

route add default gw $ENDPOINT_INTERFACE

echo "Gateway now points to $ENDPOINT_INTERFACE"