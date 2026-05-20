#!/bin/bash

kubectl apply -f cmSecret-ns.yml

if [[ $? -ne 0 ]]; then
    echo "please create namespace and configMap for mongodb App"
    kubectl apply -f cmSecret-ns.yml
else
  echo "Namespace already exist please proceed"
 #kubectl -n jjva-ns-svc-pod rollout undo deploy ${mss_pod_app}
 #echo "Deployment ${mss_pod_app} Rollout is Success"
fi
