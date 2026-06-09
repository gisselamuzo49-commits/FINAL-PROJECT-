# Authentication Service (`auth-service`) 🔐

The **Authentication Service** is a core microservice built on Spring Boot that manages user registration, user authentication, and JSON Web Token (JWT) generation. It ensures secure access control for all internal services.

---

## 🚀 Key Features

* **User Registration**: Registers new users with password hashing.
* **JWT Issuance**: Authenticates users and issues signed JWT tokens for downstream authorization.
* **SQLite Database**: Lightweight SQL storage configured for fast verification.
* **Spring Boot 4.x & Spring Security**: Modern application backbone with security-oriented defaults.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Security & Crypto** (for secure password hashing)
* **JSON Web Token (jjwt 0.13.0)** (for API token generation)
* **SQLite JDBC & Hibernate Dialect** (for database interaction)

---

## 📦 Main Directory Structure

```text
apps/auth-service/
├── src/main/java/com/uce/auth_service/
│   ├── AuthController.java        # REST Endpoint Handlers (login/register)
│   ├── models/                    # Data models representing the User schema
│   ├── repositories/              # Spring Data JPA Repository interfaces
│   └── services/                  # Business Logic services for hashing and token generation
├── Dockerfile                     # Containerization instructions
└── pom.xml                        # Project dependencies and build settings
```

---

## ⚙️ Configuration Properties

Key settings are configured in `src/main/resources/application.properties` (or mapped to environment variables):

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | Port on which the service runs. |
| `JWT_SECRET` | *(Standard 256-bit String)* | Used to sign the issued authentication tokens. |
| `spring.datasource.url` | `jdbc:sqlite:auth.db` | Path to the SQLite database. |

---

## 🔌 Exposed API Endpoints

All API endpoints are prefixed with `/api/auth`.

### 1. Hello Test
* **Endpoint**: `GET /api/auth/hello`
* **Headers**: None
* **Description**: Verifies that the service is running and responsive.
* **Response**:
  ```text
  "¡Hola desde el Backend de Pasantías (Spring Boot)!"
  ```

### 2. User Registration
* **Endpoint**: `POST /api/auth/register`
* **Body** (`application/json`):
  ```json
  {
    "email": "estudiante@uce.edu.ec",
    "password": "mySecurePassword"
  }
  ```
* **Response**: Confirmation message on success.

### 3. User Login
* **Endpoint**: `POST /api/auth/login`
* **Body** (`application/json`):
  ```json
  {
    "email": "estudiante@uce.edu.ec",
    "password": "mySecurePassword"
  }
  ```
* **Response** (`200 OK`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.ey...",
    "email": "estudiante@uce.edu.ec"
  }
  ```

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/auth-service:latest .
```

### Run Container
```bash
docker run -d \
  --name auth-service \
  -p 8080:8080 \
  -v /var/lib/pasantias/auth.db:/app/auth.db \
  gdmuzo/auth-service:latest
```
