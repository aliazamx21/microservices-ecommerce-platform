# 🛒 Microservices E-Commerce Platform

A **cloud-native, multi-cloud e-commerce backend** built with Spring Boot and modern DevOps technologies. The project demonstrates **microservices architecture, event-driven communication, AI/MCP integration, Kubernetes, Terraform, and automated CI/CD**.

## 🚀 Highlights

- 🧩 **Microservices Architecture** with Spring Boot & Spring Cloud
- 🔐 **JWT Authentication** with Spring Security
- ⚡ **Event-driven communication** using Apache Kafka
- 🔄 **OpenFeign** for synchronous service-to-service communication
- 🤖 **Google Gemini + Spring AI + MCP** for AI-powered search and agents
- 🔎 **Qdrant** for vector search and AI embeddings
- 🐳 **Docker & Docker Compose** for containerized development
- ☸️ **Kubernetes** for container orchestration
- 🏗️ **Terraform** for Infrastructure as Code
- ☁️ **AWS + Azure + GCP** multi-cloud deployment
- 🔄 **GitHub Actions** for CI/CD automation
- 📊 **ELK, Zipkin, Prometheus & Grafana** for observability
- 💳 **Stripe** integration for payments and secure webhooks
- 🗄️ **Database-per-Service** architecture with MySQL

---

## 🏗️ Architecture

```text
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   API Gateway   │
                       └────────┬────────┘
                                │
             ┌──────────────────┼──────────────────┐
             ▼                  ▼                  ▼
       ┌──────────┐       ┌───────────┐      ┌──────────┐
       │   Auth   │       │  Product  │      │   Cart   │
       │ Service  │       │  Service  │      │ Service  │
       └──────────┘       └─────┬─────┘      └──────────┘
                                │
                         ┌──────▼──────┐
                         │    Qdrant   │
                         │ Vector DB   │
                         └─────────────┘

             ┌───────────────┐       ┌────────────────┐
             │ Order Service │──────▶│     Kafka      │
             └───────────────┘       └───────┬────────┘
                                             │
                                      ┌──────▼───────┐
                                      │    Payment   │
                                      │    Service   │
                                      └──────────────┘

                         AI / MCP Layer
                    ┌─────────────────────┐
                    │    Gemini + MCP     │
                    └─────────────────────┘

                    Observability Layer
               ┌──────────────────────────┐
               │ ELK • Zipkin • Prometheus│
               │ Grafana • Spring Admin   │
               └──────────────────────────┘
```

---

## 🧩 Microservices

| Service | Responsibility |
|---|---|
| **API Gateway** | Request routing & centralized entry point |
| **Eureka Server** | Service discovery |
| **Auth Service** | Authentication & JWT |
| **Product Service** | Product catalog, S3 & AI search |
| **Cart Service** | Shopping cart management |
| **Order Service** | Order processing & events |
| **Payment Service** | Stripe checkout & webhooks |
| **MCP Client** | Gemini AI & agent integration |
| **MCP Sandbox** | MCP execution environment |

---

## 🛠️ Tech Stack

**Backend:** Java 17, Spring Boot, Spring Cloud, Spring Security, Spring AI

**Messaging:** Apache Kafka, OpenFeign

**AI:** Google Gemini, Model Context Protocol (MCP), Qdrant

**Database:** MySQL, Spring Data JPA

**Cloud:** AWS (EKS, RDS, S3), Azure (AKS), GCP (GKE Autopilot)

**DevOps:** Docker, Kubernetes, Terraform, GitHub Actions

**Observability:** ELK Stack, Zipkin, Prometheus, Grafana, Micrometer, Spring Boot Admin

**Payments:** Stripe

---

## 🚀 Quick Start

### Prerequisites

- Docker Desktop
- Git

### Run locally

```bash
git clone https://github.com/aliazamx21/microservices-ecommerce-platform.git

cd microservices-ecommerce-platform

docker compose up -d
```

Check running services:

```bash
docker ps
```

Stop the platform:

```bash
docker compose down
```

---

## ☁️ Deployment

The project supports a multi-cloud deployment strategy:

```text
AWS
├── EKS
├── RDS
├── S3
└── VPC

Azure
└── AKS

GCP
└── GKE Autopilot
      └── AI / MCP Services
```

Infrastructure is provisioned using **Terraform**, while **Kubernetes** manages application workloads.

---

## 🔄 CI/CD

GitHub Actions automates:

```text
Code Push
   ↓
Build & Test
   ↓
Docker Build
   ↓
Container Registry
   ↓
Terraform
   ↓
Kubernetes Deployment
```

---

## 📊 Observability

The platform provides centralized:

- **Logging:** Elasticsearch + Logstash + Kibana
- **Tracing:** Zipkin
- **Metrics:** Prometheus + Micrometer
- **Dashboards:** Grafana
- **Application Health:** Spring Boot Admin
- **Code Quality:** SonarQube

---

## 📐 Architecture Principles

- Database-per-Service
- API Gateway
- Service Discovery
- Event-Driven Architecture
- Asynchronous Messaging
- Stateless Authentication
- Containerized Services
- Infrastructure as Code
- Multi-Cloud Deployment
- Centralized Observability

---

## 👨‍💻 Author

**Ali Azam**

GitHub: https://github.com/aliazamx21