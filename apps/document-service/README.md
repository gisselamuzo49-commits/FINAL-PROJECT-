# Document Service

Microservicio modular y orientado a eventos (**Event-Driven**) encargado de generar certificados y documentos oficiales en formato PDF para los estudiantes, almacenándolos en AWS S3 y registrando la metadata.

El servicio consume eventos de registro de horas validadas desde Apache Kafka (`horas.registradas`), genera el PDF usando OpenPDF (iText 2.1.7), sube el archivo a un bucket S3 de AWS, guarda la metadata en PostgreSQL (`document_db`), realiza un upsert del resumen del estudiante en MongoDB (`document_read_db`), y notifica de forma asíncrona ("best effort") a un webhook de n8n.

## Detalles Técnicos
- **Puerto de ejecución:** `8088` (HTTP REST)
- **Tecnologías:** Java 17, Spring Boot 4.0.6, Spring Data JPA, Spring Data MongoDB, Spring Kafka, AWS SDK S3 v2, OpenPDF.
- **Base de datos relacional:** `document_db` (PostgreSQL - metadata)
- **Base de datos documental:** `document_read_db` (MongoDB - resumen/proyección de lectura)
- **Topic Kafka consumido:** `horas.registradas` (solo procesa eventos con estado `VALIDADO`)

---

## Variables de Enorno

| Variable | Propósito / Valor por Defecto |
| --- | --- |
| `DB_HOST` | Host de PostgreSQL (default: `localhost`) |
| `DB_PORT` | Puerto de PostgreSQL (default: `5432`) |
| `DB_NAME` | Nombre de la base de datos Postgres (default: `document_db`) |
| `DB_USER` | Usuario de base de datos Postgres (default: `postgres`) |
| `DB_PASSWORD` | Contraseña de base de datos Postgres (requerida) |
| `MONGO_URI` | Connection String de MongoDB (default: `mongodb://localhost:27017/document_read_db`) |
| `KAFKA_BOOTSTRAP_SERVERS` | Direcciones del broker de Kafka (default: `localhost:9092`) |
| `S3_BUCKET_NAME` | Nombre del bucket S3 de AWS (`pasantias-documents-qa` / `pasantias-documents-prod`) |
| `AWS_ACCESS_KEY_ID` | Access Key de AWS para S3 |
| `AWS_SECRET_ACCESS_KEY` | Secret Key de AWS para S3 |
| `AWS_SESSION_TOKEN` | Token de sesión temporal de AWS Academy |
| `N8N_WEBHOOK_URL` | URL del webhook de n8n (opcional) |

---

## Endpoints Principales

### 1. Health Check
`GET /health`
- **Respuesta (200 OK):** `"document-service is running"`

### 2. Resumen de Documentos por Estudiante (Lectura desde MongoDB)
`GET /api/documents/student/{estudianteId}`
- **Respuesta (200 OK):**
  ```json
  {
    "id": "student_99",
    "totalDocumentos": 1,
    "documentos": [
      {
        "documentoId": 1,
        "tipo": "CERTIFICADO_HORAS",
        "s3Url": "https://pasantias-documents-qa.s3.amazonaws.com/documents/student_99_501.pdf",
        "createdAt": "2026-06-17T11:24:00"
      }
    ]
  }
  ```
- **Respuesta (404 Not Found):** Si el estudiante no tiene ningún documento generado.

### 3. Metadata de Documento Individual (Lectura desde PostgreSQL)
`GET /api/documents/{id}`
- **Respuesta (200 OK):**
  ```json
  {
    "id": 1,
    "estudianteId": "student_99",
    "proyectoId": "proj-z",
    "tipo": "CERTIFICADO_HORAS",
    "s3Key": "documents/student_99_501.pdf",
    "s3Url": "https://pasantias-documents-qa.s3.amazonaws.com/documents/student_99_501.pdf",
    "estado": "GENERADO",
    "createdAt": "2026-06-17T11:24:00"
  }
  ```
- **Respuesta (404 Not Found):** Si no existe la metadata del documento con ese ID.

### 4. Documentación Swagger / OpenAPI
- **Ruta Docs:** `http://localhost:8088/api-docs`
- **Ruta Interfaz UI:** `http://localhost:8088/swagger-ui.html`

---

## Pruebas Automatizadas
Las pruebas unitarias y de carga de contexto se ejecutan simulando la base de datos H2 en memoria y mockeando las llamadas S3 y MongoDB:
```bash
./mvnw test
```
