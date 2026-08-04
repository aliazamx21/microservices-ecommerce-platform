#!/bin/bash
echo "Deploying Cloud Kafka & Prometheus/Grafana..."

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Kafka
helm upgrade --install kafka bitnami/kafka \
  --set replicaCount=1 \
  --set auth.clientProtocol=none \
  --set listeners.client.protocol=PLAINTEXT

# Install Prometheus + Grafana Dashboard
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.service.type=LoadBalancer

echo "DevOps Infrastructure Tools Ready!"