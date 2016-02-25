#!/bin/bash

<<INFO
Author				: Papaioannou Vassileios
Last update			: 28/ 09/ 2015
Previous updates	: 26/ 09/ 2015
Host System			: Ubuntu >= 10.04 LTS
INFO

<<DESCRIPTION
Color definitions to use in bash scripts
DESCRIPTION

#text formating
##emphasis
bold=`tput bold`
##colors
###foreground
black=`tput setaf 0`
red=`tput setaf 1`
green=`tput setaf 2`
darkYellow=`tput setaf 3`
blue=`tput setaf 4`
purple=`tput setaf 5`
###background
whiteb=`tput setab 7`
##reset colors to default
reset=`tput sgr0`
