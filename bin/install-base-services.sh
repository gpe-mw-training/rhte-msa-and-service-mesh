#!/bin/bash

oc apply -f <(istioctl kube-inject --debug -f ../gateway/src/main/kubernetes/Deployment.yml)
oc apply -f ../gateway/src/main/kubernetes/Service.yml
oc apply -f <(istioctl kube-inject --debug -f ../partner/src/main/kubernetes/Deployment.yml)
oc apply -f ../partner/src/main/kubernetes/Service.yml
oc apply -f <(istioctl kube-inject --debug -f ../catalog/src/main/kubernetes/Deployment.yml)
oc apply -f ../catalog/src/main/kubernetes/Service.yml

oc expose service gateway
oc get route