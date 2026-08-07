#!/bin/bash
# Exit immediately if any command fails
set -e

echo "Deploying Cloud Kafka & Prometheus/Grafana..."

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Kafka (Added --wait)
helm upgrade --install kafka bitnami/kafka \
  --set replicaCount=1 \
  --set auth.clientProtocol=none \
  --set listeners.client.protocol=PLAINTEXT \
  --wait

# Install Prometheus + Grafana Dashboard (Added --wait)
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.service.type=LoadBalancer \
  --wait

echo "DevOps Infrastructure Tools Ready!"