# Intelligent System for the Management of Pre-Professional Internships and University Outreach Projects 🚀

![Deploy QA](https://github.com/gisselamuzo49-commits/FINAL-PROJECT-/actions/workflows/deploy-qa.yml/badge.svg)
![Deploy PROD](https://github.com/gisselamuzo49-commits/FINAL-PROJECT-/actions/workflows/deploy-prod.yml/badge.svg)

This project proposes an intelligent distributed system based on a microservices architecture, integrating cloud computing, DevOps practices, and artificial intelligence to optimize academic monitoring processes and improve operational efficiency. Developed for the Central University of Ecuador (UCE), Faculty of Engineering and Applied Sciences.

---

## 🎯 System Objectives

* Design and implement a microservices-based distributed architecture.
* Implement robust authentication and authorization mechanisms (JWT, RBAC).
* Develop modules for managing internships, student evaluations, outreach projects, and hours tracking.
* Integrate AI services for student risk prediction and internship recommendations.
* Deploy automated CI/CD pipelines to AWS staging and production environments.

---

## 🧠 Artificial Intelligence (AI) Module

The system transforms manual processes into automated, data-driven workflows. It integrates the following components:

* **Skill Extraction**: Natural language processing (NLP) using the spaCy library to automatically extract key skills from students' CVs.
* **Recommendation System**: Uses the TF-IDF algorithm combined with cosine similarity to mathematically match a student profile with available internship offers.
* **Risk Prediction**: Implements the Random Forest algorithm (a supervised learning method) to identify students at risk of dropping out based on their academic history and recorded hours.

---

## 🏗️ Architecture and Technologies

The system follows a distributed microservices architecture where each service operates independently.

### Microservices Directory

* **auth-service (Port 8080)**: Manages authentication, user registration, and security tokens using Spring Boot.
* **internship-service (Port 8081)**: Handles internship offers, applications, and requirements using Spring Boot + Neo4j.
* **gateway-service (Port 8082)**: Serves as the single entry point for API routing and rate-limiting using Spring Cloud Gateway.
* **user-service (Port 8083)**: Manages student, coordinator, and tutor profiles, acting as a gRPC server using Spring Boot.
* **linkage-service (Port 8084)**: Handles university outreach projects and institutional agreements using Spring Boot.
* **hours-service (Port 8085)**: Manages the registration and validation of student hours using Spring Boot + CQRS.
* **evaluation-service (Port 8086)**: Conducts student and supervisor performance reviews using Spring Boot.
* **notification-service (Port 8087)**: Dispatches alerts and emails using Spring Boot + MQTT.
* **document-service (Port 8088)**: Handles academic document generation and storage using Spring Boot + AWS S3.
* **report-service (Port 8089)**: Generates academic metrics and statistics, exposing a SOAP endpoint using Spring Boot.
* **ai-service (Port 8090)**: Powers NLP recommendations and machine learning risk predictions using Python FastAPI.

### Technology Stack

* **Core Backend**: Java Spring Boot
* **AI Backend**: Python FastAPI
* **Frontend**: React (configured as a Progressive Web App - PWA)

### Inter-Service Communication

* **REST**: Used for synchronous communication between frontend ↔ gateway ↔ services.
* **gRPC**: Synchronous high-performance internal communication (used by `hours-service` and `evaluation-service` to consult `user-service`).
* **Apache Kafka**: Asynchronous event-driven communication (e.g., publishing `horas.registradas` events).
* **RabbitMQ**: Message queue for heavy asynchronous tasks in `ai-service`.
* **MQTT/Mosquitto**: Real-time message broker used by `notification-service` for telemetry and heartbeat metrics.
* **Webhooks**: Triggers external workflows (e.g., `document-service` calling self-hosted `n8n`).

### Polyglot Persistence

The system implements polyglot persistence to use the best-suited database engine for each scenario:

* **PostgreSQL**: Relational database engine (9 independent databases: `auth_db`, `internship_db`, `user_db`, `linkage_db`, `hours_db`, `evaluation_db`, `notification_db`, `document_db`, `report_db`).
* **MongoDB**: Document-oriented database used for storing flexible CQRS read models in `hours-service`, `document-service`, and `report-service`.
* **Redis**: High-speed key-value cache used for JWT token validation and API gateway rate-limiting.
* **Neo4j**: Graph database used for mapping relationships between Student ↔ Application ↔ Internship Offer in `internship-service`.

---

## 🔗 Access Links (Environments)

### QA Environment
* **QA Frontend**: [http://50.19.247.85](http://50.19.247.85)
* **QA Gateway**: [http://50.19.247.85:8082](http://50.19.247.85:8082)

### Production Environment (PROD)
* **PROD**: [pasantias-prod-elb-1617123986.us-east-1.elb.amazonaws.com](http://pasantias-prod-elb-1617123986.us-east-1.elb.amazonaws.com)

---

## 🚀 Local Deployment Guide (Development Environment)

Due to the computational resource limits of the AWS Free Tier (t2.micro instances), local execution is the recommended environment for development, debugging, and real-time demonstration of inter-service communication.

To run the project in your local environment, open three terminals in the root of the project and execute:

### 1. Run Auth-Service (Port 8080)

```bash
cd apps/auth-service
./mvnw spring-boot:run
```

### 2. Run Internship-Service (Port 8081)

```bash
cd apps/internship-service
./mvnw spring-boot:run
```

### 3. Run Frontend Web (Port 5173)

```bash
cd apps/frontend-web
npm install
npm run dev
```

Once running, access [http://localhost:5173](http://localhost:5173) in your browser. The services are globally configured with CORS to allow REST request interoperability.

---

## ☁️ Cloud Deployment (AWS)

The project includes automation using Terraform and the GitOps paradigm. 

### QA Environment Deployment

By merging/pushing code to the `QA` branch, a GitHub Actions workflow (`deploy-qa.yml`) is automatically triggered to:
1. Build and tag the Docker images (e.g., `gdmuzo/auth-service:qa`, `gdmuzo/internship-service:qa`, `gdmuzo/frontend-web:qa`).
2. Push the Docker images to Docker Hub.
3. Initialize and apply the Terraform configuration located in the `./infra/qa` directory to provision/update AWS resources.

> [!NOTE]
> **Technical Note**: For production environments, it is recommended to scale the EC2 instance or migrate Java microservices to managed containers (ECS/EKS) to avoid RAM bottlenecks during compilation.

---

## 👥 Authorship

* **Student**: Gissela Muzo
* **Institution**: Universidad Central del Ecuador - FICA
* **Career**: Sistemas de Información
* **Tutor**: Ing. Juan Pablo Guevara
* **Year**: 2026
