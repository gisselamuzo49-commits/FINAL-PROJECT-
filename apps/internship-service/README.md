# Internship Service (`internship-service`) 💼

The **Internship Service** is a Spring Boot microservice that handles the publication, listing, and lifecycle management of pre-professional internship offers. It interfaces with PostgreSQL for persistence.

---

## 🚀 Key Features

* **Create Internships**: Exposes endpoints to post new internship offers with details like company name, title, description, and status.
* **List Internships**: Retrieves available internship positions.
* **Secure Communications**: Downstream requests are validated at the Gateway.
* **Robust Storage**: PostgreSQL-backed JPA layer.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA**
* **PostgreSQL JDBC & Hibernate Dialect**

---

## 📦 Main Directory Structure

```text
apps/internship-service/
├── src/main/java/com/uce/internship_service/
│   ├── InternshipServiceApplication.java   # Service entry point
│   ├── controllers/                        # Internship REST endpoints
│   ├── models/                             # Internship model entity
│   ├── repositories/                       # Spring Data JPA Repository
│   └── services/                           # Business logic service class
├── Dockerfile                              # Container setup
└── pom.xml                                 # Maven dependencies
```

---

## ⚙️ Configuration Properties

Key settings in `src/main/resources/application.properties`:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8081` | Port on which the service runs. |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/internship_db` | Connection URL for the PostgreSQL database. |

---

## 🔌 API Endpoints

Endpoints are routed through the API Gateway, which requires a valid JWT for authentication.

### 1. Retrieve All Internship Offers
* **Endpoint**: `GET /api/internships`
* **Security**: Enforced JWT authorization header.
* **Response**: List of internships.
  ```json
  [
    {
      "id": 1,
      "title": "Java Developer Intern",
      "company": "Tech Solutions Corp",
      "description": "Looking for a student to assist with Java API development.",
      "status": "ABIERTA"
    }
  ]
  ```

### 2. Create Internship Offer
* **Endpoint**: `POST /api/internships`
* **Security**: Enforced JWT authorization header.
* **Body** (`application/json`):
  ```json
  {
    "title": "Java Developer Intern",
    "company": "Tech Solutions Corp",
    "description": "Looking for a student to assist with Java API development.",
    "status": "ABIERTA"
  }
  ```
* **Response**: The saved internship object containing the auto-generated ID.

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/internship-service:latest .
```

### Run Container
```bash
docker run -d \
  --name internship-service \
  --network pasantias-net \
  -p 8081:8081 \
  -e DB_HOST=postgres-db \
  -e DB_PORT=5432 \
  -e DB_NAME=internship_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD="<secret-managed-outside-source-control>" \
  gdmuzo/internship-service:latest
```
