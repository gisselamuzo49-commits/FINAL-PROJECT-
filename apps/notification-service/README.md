# Notification Service

Microservicio modular y distribuido orientado a eventos (**Event-Driven**) encargado de gestionar y propagar notificaciones a los estudiantes.

El servicio consume eventos de registro de horas desde Apache Kafka (`horas.registradas`), guarda el registro de la notificación en PostgreSQL (`notification_db`) y publica en tiempo real a través del broker MQTT (**HiveMQ Cloud**) mediante cifrado seguro TLS en un topic dinámico por estudiante.

## Detalles Técnicos
- **Puerto de ejecución:** `8087` (HTTP REST)
- **Tecnologías:** Java 17, Spring Boot 4.0.6, Spring Data JPA, Spring Kafka, Paho MQTT Client, PostgreSQL.
- **Base de datos:** `notification_db` (PostgreSQL)
- **Topic Kafka consumido:** `horas.registradas` (escucha validaciones y rechazos de horas)
- **Topic MQTT publicado:** `notificaciones/{estudianteId}` (MQTT over TLS, puerto `8883` en HiveMQ Cloud)

---

## Variables de Entorno

| Variable | Propósito / Valor por Defecto |
| --- | --- |
| `DB_HOST` | Host de PostgreSQL (default: `localhost`) |
| `DB_PORT` | Puerto de PostgreSQL (default: `5432`) |
| `DB_NAME` | Nombre de la base de datos (default: `notification_db`) |
| `DB_USER` | Usuario de base de datos (default: `postgres`) |
| `DB_PASSWORD` | Contraseña de base de datos (default: `postgres`) |
| `KAFKA_BOOTSTRAP_SERVERS` | Direcciones del broker de Kafka (default: `localhost:9092`) |
| `MQTT_HOST` | Host del broker MQTT HiveMQ Cloud (default: `58567514d2724107bcd88d74e13a8bd8.s1.eu.hivemq.cloud`) |
| `MQTT_PORT` | Puerto MQTT TLS (default: `8883`) |
| `MQTT_USERNAME` | Usuario del broker MQTT (default: `mqttuser`) |
| `MQTT_PASSWORD` | Contraseña del broker MQTT |
| `MQTT_CLIENT_ID` | Identificador de cliente MQTT (default: `notification-service-client`) |

---

## Endpoints Principales

### 1. Health Check
`GET /health`
- **Respuesta (200 OK):** `"notification-service is running"`

### 2. Notificaciones de Estudiante
`GET /api/notifications/student/{estudianteId}`
- **Descripción:** Retorna el listado de notificaciones recibidas por el estudiante, ordenadas de la más reciente a la más antigua.
- **Respuesta (200 OK):**
  ```json
  [
    {
      "id": 1,
      "estudianteId": "student_42",
      "mensaje": "Se han validado sus horas para el proyecto Proyecto Vinculacion UCE.",
      "tipo": "HORAS_VALIDADAS",
      "horasId": 101,
      "leida": false,
      "createdAt": "2026-06-17T15:27:00"
    }
  ]
  ```

### 3. Marcar como Leída
`PATCH /api/notifications/{id}/read`
- **Descripción:** Marca la notificación indicada con el `id` como leída (`leida=true`) y notifica la actualización al broker MQTT.
- **Respuesta (200 OK):** Objeto `Notificacion` actualizado.
- **Respuesta (404 Not Found):** Si la notificación no existe.

### 4. Documentación Swagger / OpenAPI
- **Ruta Docs:** `http://localhost:8087/api-docs`
- **Ruta Interfaz UI:** `http://localhost:8087/swagger-ui.html`

---

## Pruebas Automatizadas
Las pruebas corren con Testcontainers levantando bases de datos PostgreSQL y Kafka reales temporalmente:
```bash
./mvnw test
```
