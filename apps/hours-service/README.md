# Hours Service (`hours-service`) ⏱️

The **Hours Service** is a Spring Boot microservice responsible for registering and
validating the hours that students dedicate to internship and community linkage activities.
It is the first service in the project implementing the **CQRS** pattern (Command Query
Responsibility Segregation) — this initial version covers the **command side** (PostgreSQL
writes via REST). The query side (MongoDB read model via Kafka) will be added in a
subsequent stage.

---

## 🚀 Key Features

* **Register Hours**: Students submit hours worked on a project (POST).
* **Validate Hours**: Tutors approve or reject submitted hours (PATCH).
* **Status Tracking**: Each record follows the lifecycle `PENDIENTE → VALIDADO / RECHAZADO`.
* **PostgreSQL Backend**: Independent persistence using `hours_db`.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA**
* **PostgreSQL JDBC & Hibernate Dialect**

---

## 📦 Main Directory Structure

```text
apps/hours-service/
├── src/main/java/com/uce/hours_service/
│   ├── HoursServiceApplication.java     # Main entry point & health endpoint
│   ├── HoursController.java             # REST endpoint definitions
│   ├── models/
│   │   ├── RegistroHoras.java           # JPA entity (registro_horas table)
│   │   └── EstadoHoras.java             # Enum: PENDIENTE, VALIDADO, RECHAZADO
│   ├── repositories/
│   │   └── RegistroHorasRepository.java # JPA repository interface
│   └── services/
│       └── HoursService.java            # Business logic
├── src/test/java/com/uce/hours_service/
│   └── HoursControllerTest.java         # Unit tests (MockMvc + Mockito)
├── Dockerfile
└── pom.xml
```

---

## ⚙️ Configuration Properties

Key settings in `src/main/resources/application.properties`:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8085` | Port on which the service runs. |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/hours_db` | Connection URL for the PostgreSQL database. |

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `hours_db` | Database name |
| `DB_USER` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |

---

## 🔌 API Endpoints

### 1. Health Status
* **Endpoint**: `GET /health`
* **Security**: Public (no JWT required).
* **Response**:
  ```text
  "hours-service is running"
  ```

### 2. Register Hours
* **Endpoint**: `POST /api/hours`
* **Body** (`application/json`):
  ```json
  {
    "estudianteId": "1",
    "proyectoId": "10",
    "fecha": "2026-06-10",
    "horas": 4.5,
    "descripcionActividad": "Taller de capacitación comunitaria"
  }
  ```
* **Response** (`200 OK`): The saved record with `estado=PENDIENTE` and `createdAt` auto-set.
* **Errors**: `400 Bad Request` with descriptive message if any required field is missing.

### 3. Validate Hours
* **Endpoint**: `PATCH /api/hours/{id}/validar`
* **Body** (`application/json`):
  ```json
  {
    "tutorId": "5",
    "aprobado": true
  }
  ```
* **Response** (`200 OK`): The updated record with `estado=VALIDADO` or `RECHAZADO`.
* **Errors**:
  - `404 Not Found` if the record ID does not exist.
  - `400 Bad Request` if `tutorId` or `aprobado` is missing.

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/hours-service:latest .
```

### Run Container
```bash
docker run -d \
  --name hours-service \
  --network pasantias-net \
  -p 8085:8085 \
  -e DB_HOST=postgres-db \
  -e DB_PORT=5432 \
  -e DB_NAME=hours_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  gdmuzo/hours-service:latest
```

---

## 🗺️ Roadmap (subsequent stages)

- **Stage 2**: Kafka producer — publish `horas.registradas` event after each `save()`.
- **Stage 3**: Kafka consumer + MongoDB read model + `GET /api/hours/student/{id}`.
- **Stage 4**: gRPC client to `user-service` to enrich the read model with student name/carrera.
