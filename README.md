# 🚀 Distributed Microservices E-Commerce Platform

A robust, enterprise-grade e-commerce backend platform built using **Java, Spring Boot, and Spring Cloud**. This system uses a microservices architecture to handle user authentication, product management, shopping carts, order processing, and secure payment integrations.

---

## 🛠️ Tech Stack & Architecture Components

* **Java / Spring Boot:** Core application development framework.
* **Spring Cloud Netflix Eureka:** Service discovery registry for dynamic microservices communication.
* **Spring Cloud API Gateway:** Single entry point handling routing, filtering, and security.
* **Spring Security & JWT:** Stateless authentication and authorization across services.
* **MySQL & JPA/Hibernate:** Relational database management for each individual microservice.
* **Apache Kafka / OpenFeign:** Synchronous/Asynchronous inter-service communication.
* **AWS S3:** Cloud storage integration for product images and media assets.
* **Stripe API:** Secure payment processing gateway.

---

## 🏗️ Microservices Ecosystem

1. **`eureka-server`** - Acts as the service registry where all microservices register themselves dynamically.
2. **`api-gateway`** - The entry point for all client requests; handles routing and intercepts requests for JWT validation.
3. **`authservice`** - Manages user registration, login, and generates secure JWT tokens.
4. **`product-service`** - Manages product catalogs, categories, brands, and integrates with AWS S3 for media storage.
5. **`cart-service`** - Handles user shopping cart operations (adding/updating items).
6. **`order-service`** - Manages checkout logic, order placement, and communicates with the cart service via OpenFeign.
7. **`payment-service`** - Integrates with Stripe to handle secure online transactions and order status updates.

---

## 🔄 System Request & Architecture Flow (Interview Guide)

When an interviewer asks about your system design flow, explain it through this end-to-end user journey:

1. **Authentication:**
    - The user sends credentials to `authservice` via the `api-gateway`.
    - Upon successful verification, a **JWT token** is issued back to the client.
2. **Routing & Gateway Filtering:**
    - Subsequent requests from the client carry the JWT token in the header and hit the **API Gateway**.
    - The Gateway's custom `AuthenticationFilter` validates the JWT token before routing the request to downstream services.
3. **Service Discovery:**
    - The API Gateway uses **Eureka Server** to look up the dynamic network locations of internal microservices (e.g., `product-service`, `order-service`) rather than hardcoding IP addresses.
4. **Checkout & Inter-Service Communication:**
    - When a user checks out, the `order-service` triggers a call to the `cart-service` (using **OpenFeign**) to fetch the user's current cart items.
    - Once items are retrieved, an order is created with a `PENDING` status.
5. **Payment Processing:**
    - The `payment-service` takes the order details, communicates with the **Stripe API** to process the payment securely, and updates the final order status upon success.

---

## 📂 Database Design
An ER diagram (`ER_Diagram.mwb`) is included in the repository outlining the relational database schemas for users, products, carts, orders, and payments following a **Database-per-Service** microservices pattern.