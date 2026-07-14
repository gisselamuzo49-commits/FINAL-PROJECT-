# Servicio de Evaluación Final (`evaluation-service`) 📝

El **Servicio de Evaluación Final** es un microservicio construido en Java 17 + Spring Boot que gestiona el registro y consulta de las evaluaciones finales de los estudiantes que realizan pasantías preprofesionales y proyectos de vinculación con la sociedad en la Universidad Central del Ecuador (UCE).

---

## 🚀 Propósito del Servicio

Permite que tutores evalúen de forma final a los estudiantes, registrando calificaciones (entre 0 y 10 inclusive) y comentarios de desempeño, interactuando de forma no bloqueante ("best effort") con el `user-service` vía gRPC para enriquecer las consultas de evaluación con los datos del estudiante (nombre, apellido y carrera).

---

## 🔌 API Endpoints y Contrato

Todos los endpoints (a excepción de `/health`) están expuestos en el puerto `8086`.

### 1. Health Check (Salud del Servicio)
* **Ruta**: `GET /health`
* **Código de respuesta**: `200 OK`
* **Cuerpo de respuesta**:
  ```text
  evaluation-service is running
  ```

### 2. Registrar Evaluación Final
* **Ruta**: `POST /api/evaluations`
* **Cuerpo de Petición** (`application/json`):
  ```json
  {
    "estudianteId": "101",
    "proyectoId": "50",
    "tutorId": "202",
    "fechaEvaluacion": "2026-06-15",
    "calificacion": 8.50,
    "comentarios": "Excelente desempeño técnico y puntualidad."
  }
  ```
* **Validaciones**:
  * `estudianteId`: Obligatorio. Retorna `400 Bad Request` si falta.
  * `tutorId`: Obligatorio. Retorna `400 Bad Request` si falta.
  * `proyectoId`: Obligatorio. Retorna `400 Bad Request` si falta.
  * `calificacion`: Obligatorio y debe estar en el rango de `0` a `10` inclusive. Retorna `400 Bad Request` si falta o si está fuera de rango.

* **Respuestas**:
  * `200 OK`: Evaluación creada con éxito.
  * `400 Bad Request`: Parámetro requerido faltante o calificación fuera de rango (0-10).

### 3. Obtener Evaluación por ID
* **Ruta**: `GET /api/evaluations/{id}`
* **Respuestas**:
  * `200 OK`: Si la evaluación existe. Retorna el detalle completo.
  * `404 Not Found`: Si el ID especificado no se encuentra registrado.

### 4. Listar Evaluaciones por Estudiante
* **Ruta**: `GET /api/evaluations/student/{estudianteId}`
* **Respuestas**:
  * `200 OK`: Retorna la lista de evaluaciones del estudiante enriquecida con `nombre` (nombre + apellido) y `carrera` obtenidos vía cliente gRPC.
* **Cuerpo de respuesta**:
  ```json
  [
    {
      "id": 1,
      "estudianteId": "101",
      "proyectoId": "50",
      "tutorId": "202",
      "fechaEvaluacion": "2026-06-15",
      "calificacion": 8.50,
      "comentarios": "Excelente desempeño técnico y puntualidad.",
      "createdAt": "2026-06-15T12:00:00",
      "nombre": "Gissela Muzo",
      "carrera": "Computación"
    }
  ]
  ```
  _Nota: Si la llamada gRPC al `user-service` falla, los campos `nombre` y `carrera` se retornarán como `null` ("best effort")._

---

## ⚙️ Variables de Entorno (Configuración)

El servicio lee la siguiente configuración a través de variables de entorno:

| Variable de Entorno | Valor por Defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Dirección de la base de datos PostgreSQL. |
| `DB_PORT` | `5432` | Puerto del servidor de base de datos PostgreSQL. |
| `DB_NAME` | `evaluation_db` | Nombre de la base de datos de evaluación. |
| `DB_USER` | `postgres` | Nombre de usuario de conexión. |
| `DB_PASSWORD` | `postgres` | Contraseña de conexión. |
| `USER_SERVICE_GRPC_HOST` | `localhost` | Host del servicio de usuarios gRPC. |
| `USER_SERVICE_GRPC_PORT` | `9083` | Puerto del servidor gRPC del servicio de usuarios. |

---

## 🐳 Despliegue con Docker

### Construir Imagen
```bash
docker build -t gdmuzo/evaluation-service:latest .
```

### Ejecutar Contenedor
```bash
docker run -d \
  --name evaluation-service \
  --network pasantias-net \
  -p 8086:8086 \
  -e DB_HOST=postgres-db \
  -e DB_PORT=5432 \
  -e DB_NAME=evaluation_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD="<secret-managed-outside-source-control>" \
  -e USER_SERVICE_GRPC_HOST=user-service \
  -e USER_SERVICE_GRPC_PORT=9083 \
  gdmuzo/evaluation-service:latest
```

---

## 🧪 Pruebas Unitarias
Para ejecutar las pruebas:
```bash
./mvnw test
```
