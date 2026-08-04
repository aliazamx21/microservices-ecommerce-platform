# 🛒 Microservices E-Commerce Platform

A cloud-native e-commerce backend built with **Spring Boot**, **Kafka**, **Docker**, **Kubernetes**, **Terraform**, and **AWS**. The project demonstrates a scalable microservices architecture with secure authentication, asynchronous messaging, centralized observability, and automated CI/CD.

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop

### Run the project

```bash
git clone https://github.com/aliazamx21/microservices-ecommerce-platform.git
cd microservices-ecommerce-platform
docker-compose up -d
```

This starts all microservices, databases, Kafka, and the observability stack.

---

## 🛠 Tech Stack

| Category | Technologies |
|-----------|--------------|
| Backend | Java 17, Spring Boot, Spring Cloud Gateway, Eureka |
| Security | Spring Security, JWT |
| Database | MySQL, Spring Data JPA |
| Messaging | Apache Kafka, OpenFeign |
| Cloud | AWS (EKS, RDS, S3, VPC), Terraform |
| DevOps | Docker, Docker Compose, Kubernetes, GitHub Actions |
| Observability | ELK Stack, Zipkin, Micrometer |

---

## 🏗 Microservices

| Service | Responsibility |
|----------|----------------|
| `api-gateway` | API Gateway & request routing |
| `eureka-server` | Service discovery |
| `authservice` | Authentication & JWT |
| `product-service` | Product & category management |
| `cart-service` | Shopping cart operations |
| `order-service` | Order processing |
| `payment-service` | Stripe payment integration |

---

## 🔄 Architecture Flow

1. User authenticates and receives a JWT.
2. Requests are routed through the API Gateway.
3. Eureka performs service discovery.
4. Order Service communicates with Cart Service using OpenFeign.
5. Payment events are processed asynchronously through Kafka.
6. Zipkin traces requests while ELK centralizes application logs.

---

## ☁️ Deployment

- **Docker Compose** for local development
- **Kubernetes** manifests in `/k8s`
- **Terraform** infrastructure in `/terraform`
- **GitHub Actions** for automated build and deployment

---

## 📊 Observability

| Tool | Purpose |
|------|---------|
| Zipkin | Distributed tracing |
| Elasticsearch | Log storage |
| Logstash | Log processing |
| Kibana | Log visualization |

---

## 📂 Database

The repository includes an **ER diagram** (`ER_Diagram.mwb`) following the **Database-per-Service** architecture.