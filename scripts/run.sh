#!/bin/bash

for i in $(seq 10); do
  curl $GATEWAY_SERVICE_URL
done
