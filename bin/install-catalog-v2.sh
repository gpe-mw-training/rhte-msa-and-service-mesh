#!/bin/bash

oc apply -f <(istioctl kube-inject --debug -f ../catalog/src/main/kubernetes/Deployment-v2.yml)
