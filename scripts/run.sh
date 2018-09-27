#!/bin/bash

numberOfRequests=$1

if [ $# -eq 0 ]; then
	let "numberOfRequests=100"
else
	let "i = 0"
	while [ $i -lt $numberOfRequests ]; do	
	  curl $GATEWAY_URL
	  let "i=$((i + 1))"
	done
fi 