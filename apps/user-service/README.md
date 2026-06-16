# User Profile Service (`user-service`) 👤

The **User Profile Service** is a Spring Boot microservice responsible for maintaining detailed profiles of students, tutors, and career coordinators. It tracks contact details, academic roles, and identity metrics, utilizing PostgreSQL for persistence.

---

## 🚀 Key Features

* **Create User Profiles**: Registers descriptive user details mapping name, role, email, and phone.
* **List User Profiles**: Displays all registered profiles in the system.
* **Security Middleware Integration**: Gateway enforces JWT validations for endpoints.
* **PostgreSQL Backend**: Independent persistence using `user_db`.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA**
* **PostgreSQL JDBC & Hibernate Dialect**

---

## 📦 Main Directory Structure

```text
apps/user-service/
├── src/main/java/com/uce/user_service/
│   ├── UserServiceApplication.java      # Main entry point & health handler
│   ├── UserController.java            # REST endpoint definitions
│   ├── models/                          # User profile data class
│   ├── repositories/                    # Repository interface
│   └── services/                        # Business logic handler class
├── Dockerfile                           # Container configuration
└── pom.xml                              # Build dependencies
```

---

## ⚙️ Configuration Properties

Key settings in `src/main/resources/application.properties`:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8083` | Port on which the service runs. |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/user_db` | Connection URL for the PostgreSQL database. |

---

## 🔌 API Endpoints

Endpoints are protected and must be accessed via the API Gateway.

### 1. Health Status
* **Endpoint**: `GET /api/users/health` (Exposed publicly via rewrite in the gateway)
* **Response**:
  ```text
  "user-service is running"
  ```

### 2. Retrieve All User Profiles
* **Endpoint**: `GET /api/users`
* **Security**: Enforced JWT authorization header.
* **Response**: List of profiles.
  ```json
  [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@uce.edu.ec",
      "phone": "0998765432",
      "role": "STUDENT"
    }
  ]
  ```

### 3. Create User Profile
* **Endpoint**: `POST /api/users`
* **Security**: Enforced JWT authorization header.
* **Body** (`application/json`):
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@uce.edu.ec",
    "phone": "0998765432",
    "role": "STUDENT"
  }
  ```
* **Response**: The saved profile details including the assigned database ID.

### 4. gRPC Service (Internal)
* **Port**: `9083` (configurable via `grpc.server.port` / `GRPC_SERVER_PORT`)
* **Service**: `user.UserService`
* **Method**: `rpc GetStudentInfo(StudentRequest) returns (StudentInfo)`

#### Protocol Buffers Definition (`user.proto`):
```protobuf
syntax = "proto3";
package user;

service UserService {
  rpc GetStudentInfo(StudentRequest) returns (StudentInfo);
}

message StudentRequest {
  string estudiante_id = 1;
}

message StudentInfo {
  string id = 1;
  string nombre = 2;
  string apellido = 3;
  string carrera = 4;
  bool encontrado = 5;
}
```

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/user-service:latest .
```

### Run Container
```bash
docker run -d \
  --name user-service \
  --network pasantias-net \
  -p 8083:8083 \
  -p 9083:9083 \
  -e DB_HOST=postgres-db \
  -e DB_PORT=5432 \
  -e DB_NAME=user_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e GRPC_SERVER_PORT=9083 \
  gdmuzo/user-service:latest
```

