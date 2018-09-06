#!/bin/bash

namespace=$1

if [ -z "$namespace" ]; then
    namespace="tutorial"
fi

contentvs=`oc get virtualservice -n "$namespace" --as=system:admin 2>/dev/null` 

if [ -z "$contentvs" ]; then
    echo "No Virtual Services in $namespace namespace."
else
    contentvs=`awk 'NR>1' <<< "$contentvs"`

    names=`awk -v namespace="$namespace" '{ {print $1} }' <<< "$contentvs"`

    for name in "${names[@]}"
    do
        oc delete virtualservice "$name" -n "$namespace" --as=system:admin
    done
    
fi

contentdr=`oc get destinationrule -n "$namespace" --as=system:admin 2>/dev/null`

if [ -z "$contentdr" ]; then
    echo "No Destination Rule in $namespace namespace."
else
    contentdr=`awk 'NR>1' <<< "$contentdr"`

    names=`awk -v namespace="$namespace" '{ {print $1} }' <<< "$contentdr"`

    for name in "${names[@]}"
    do
        oc delete destinationrule "$name" -n "$namespace" --as=system:admin
    done
    
fi

contentse=`oc get serviceentry -n "$namespace" --as=system:admin 2>/dev/null`

if [ -z "$contentse" ]; then
    echo "No Service Entry in $namespace namespace."
else
    contentse=`awk 'NR>1' <<< "$contentse"`

    names=`awk -v namespace="$namespace" '{ {print $1} }' <<< "$contentse"`

    for name in "${names[@]}"
    do
        oc delete serviceentry "$name" -n "$namespace" --as=system:admin
    done
    
fi