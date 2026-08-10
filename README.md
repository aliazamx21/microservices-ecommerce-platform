# 🛒 Microservices E-Commerce Platform

A cloud-native e-commerce backend built with **Spring Boot**, **Kafka**, **Docker**, **Kubernetes**, **Terraform**, and **AWS**. The project demonstrates a scalable microservices architecture with secure authentication, asynchronous messaging, centralized observability, and automated CI/CD.

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop

### Run the project locally

```bash
git clone [https://github.com/aliazamx21/microservices-ecommerce-platform.git](https://github.com/aliazamx21/microservices-ecommerce-platform.git)
cd microservices-ecommerce-platform
docker-compose up -d

This starts all microservices, databases, Kafka, and the observability stack using unified container tags.🛠 Tech StackCategoryTechnologiesBackendJava 17, Spring Boot, Spring Cloud Gateway, EurekaSecuritySpring Security, JWTDatabaseMySQL, Spring Data JPAMessagingApache Kafka, OpenFeignCloudAWS (EKS, RDS, S3, VPC), TerraformDevOpsDocker, Docker Compose, Kubernetes, GitHub ActionsObservabilityELK Stack, Zipkin, Micrometer🏗 Microservices & Single Repository StructureAll e-commerce microservices are containerized under a single Docker Hub repository (aliazamx21/ecommerce-platform) using service-specific tags:ServiceDocker TagResponsibilityapi-gateway:api-gatewayAPI Gateway & request routingeureka-server:eureka-serverService discoveryauthservice:authserviceAuthentication & JWTproduct-service:product-serviceProduct & category managementcart-service:cart-serviceShopping cart operationsorder-service:order-serviceOrder processingpayment-service:payment-serviceStripe payment integration🔄 Architecture FlowUser authenticates and receives a JWT.Requests are routed through the API Gateway.Eureka performs service discovery.Order Service communicates with Cart Service using OpenFeign.Payment events are processed asynchronously through Kafka.Zipkin traces requests while ELK centralizes application logs.☁️ Deployment & AutomationDocker Compose for seamless local developmentKubernetes manifests in /k8sTerraform infrastructure code in /terraformGitHub Actions for automated infrastructure provisioning, tool deployment, and smart change-detection microservice deployment📊 ObservabilityToolPurposeZipkinDistributed tracingElasticsearchLog storageLogstashLog processingKibanaLog visualization📂 DatabaseThe repository includes an ER diagram (ER_Diagram.mwb) following the Database-per-Service architecture.