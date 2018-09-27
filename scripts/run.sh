#!/bin/bash

# if no params passed in, just do a continuous/infinite loop
# else if we have a param then it is number of requests

numberOfRequests=$1

if [ $# -eq 0 ]; then

	while true; do 
	  curl $GATEWAY_URL		
	done
else
	let "i = 0"
	while [ $i -lt $numberOfRequests ]; do	
	  curl $GATEWAY_URL
	  let "i=$((i + 1))"
	done
fi 