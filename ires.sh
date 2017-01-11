#!/bin/bash

ARG=$1
CMD="$IRES_HOME/asap-platform/asap-server/src/main/scripts/asap-server $ARG"

case "$ARG" in
	'start') 
		echo "Starting IReS Server"
		;;

	'stop')
		echo "Stopping IReS Server"
		;;

	'status')
		echo "Stopping IReS Server"
		;;

	'restart')
		echo "Restarting IReS Server"
		;;

	*) 
		echo "Invalid argument"
		;;
esac

$CMD