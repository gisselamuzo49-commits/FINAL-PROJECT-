# Hours Service (`hours-service`) ⏱️

The **Hours Service** is a Spring Boot microservice responsible for registering and validating the hours that students dedicate to internship and community linkage activities.

It is implemented using the **CQRS** pattern (Command Query Responsibility Segregation) to separate command writes and query reads for optimal scalability:
* **Command Side**: REST APIs write command data to a relational PostgreSQL database (`hours_db`).
* **Event-Driven Messaging**: Every hours registration or validation event emits a message to a Kafka topic (`horas.registradas`).
* **Query Side**: A Kafka listener processes the events, query the `user-service` via gRPC (best-effort) to retrieve student context, and updates a projection model optimized for reads stored in a MongoDB database (`hours_read_db`).

---

## 🚀 Key Features

* **Register Hours (Command)**: Students submit hours worked on a project. Automatically publishes to Kafka.
* **Validate Hours (Command)**: Tutors approve or reject submitted hours. Automatically publishes to Kafka.
* **CQRS Read Projection**: Kafka listener (`@KafkaListener`) consumes hours events, processes the history entries, and upserts a read model in MongoDB.
* **gRPC Client Integration**: Fetches student context (`nombre` and `carrera`) from `user-service` on the first registration event to enrich the projection.
* **Swagger/OpenAPI Documentation**: Exposes API documentation at `/swagger-ui.html` with global JWT Bearer protection.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA & PostgreSQL** (Command persistence)
* **Spring Data MongoDB** (Query projection persistence)
* **Spring Kafka** (Producers & Consumers)
* **gRPC / Protocol Buffers** (Client connection to `user-service` on port 9083)
* **Springdoc OpenAPI / Swagger UI**

---

## ⚙️ Configuration Properties

Key settings in `src/main/resources/application.properties`:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8085` | Port on which the service runs. |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/hours_db` | Connection URL for the PostgreSQL database. |
| `spring.mongodb.uri` | `${MONGO_URI:mongodb://localhost:27017/hours_read_db}` | Connection URI for the MongoDB database. |
| `spring.kafka.bootstrap-servers` | `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}` | Kafka broker bootstrap servers connection URL. |
| `grpc.client.user-service.address` | `static://${USER_SERVICE_GRPC_HOST:localhost}:${USER_SERVICE_GRPC_PORT:9083}` | Address of the gRPC server in `user-service`. |

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `hours_db` | Database name |
| `DB_USER` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `MONGO_URI` | `mongodb://localhost:27017/hours_read_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap brokers |
| `USER_SERVICE_GRPC_HOST` | `localhost` | `user-service` gRPC host |
| `USER_SERVICE_GRPC_PORT` | `9083` | `user-service` gRPC port |

---

## 🔌 API Endpoints

### 1. Health Status
* **Endpoint**: `GET /health`
* **Security**: Public.
* **Response**: `"hours-service is running"`

### 2. Register Hours (Command)
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
* **Response**: The saved PostgreSQL entity with `estado=PENDIENTE`.
* **Triggers**: Publishes event `horas.registradas` with key `estudianteId` to Kafka.

### 3. Validate Hours (Command)
* **Endpoint**: `PATCH /api/hours/{id}/validar`
* **Body** (`application/json`):
  ```json
  {
    "tutorId": "5",
    "aprobado": true
  }
  ```
* **Response**: The updated PostgreSQL entity with `estado=VALIDADO` or `RECHAZADO`.
* **Triggers**: Publishes event `horas.registradas` with key `estudianteId` to Kafka.

### 4. Query Student Summary (Query - MongoDB)
* **Endpoint**: `GET /api/hours/student/{estudianteId}`
* **Response** (`application/json`):
  ```json
  {
    "estudianteId": "1",
    "nombre": "Juan Perez",
    "carrera": "Sistemas",
    "totalHorasValidadas": 4.5,
    "totalHorasPendientes": 0.0,
    "historial": [
      {
        "registroId": "1",
        "proyectoId": "10",
        "fecha": "2026-06-10",
        "horas": 4.5,
        "estado": "VALIDADO",
        "descripcionActividad": "Taller de capacitación comunitaria"
      }
    ]
  }
  ```
* **Errors**: `404 Not Found` if no summary exists for the student.

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
  -e MONGO_URI=mongodb://mongodb:27017/hours_read_db \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e USER_SERVICE_GRPC_HOST=user-service \
  -e USER_SERVICE_GRPC_PORT=9083 \
  gdmuzo/hours-service:latest
```
