# Servicio de Vinculación con la Sociedad (`linkage-service`) 🔗

El **Servicio de Vinculación con la Sociedad** es un microservicio construido en Java 17 + Spring Boot que gestiona la planificación, registro y seguimiento de los proyectos de vinculación con la comunidad de la Universidad Central del Ecuador (UCE). Permite dar seguimiento a la colaboración con instituciones externas, el estado del proyecto y sus descripciones.

---

## 🚀 Propósito del Servicio

Permite que coordinadores y estudiantes administren los proyectos de vinculación de la universidad, almacenando los datos de las instituciones aliadas, los nombres y la descripción técnica de los proyectos, y el estado de desarrollo del proyecto.

---

## 🔌 API Endpoints y Contrato

Todos los endpoints (a excepción de `/health` o `/health-check`) están protegidos y deben consumirse a través del **API Gateway** (`gateway-service` en el puerto `8082`), bajo la ruta `/api/linkage/**`. Estas peticiones requieren una cabecera de autorización JWT válida y están sujetas a políticas de **Rate Limiting** (límite de peticiones) gestionado por Redis.

### 1. Health Check (Salud del Servicio)
* **Ruta local**: `GET /health` (puerto `8084`)
* **Ruta Gateway**: `GET /api/linkage/health` (Exhibida públicamente)
* **Código de respuesta**: `200 OK`
* **Cuerpo de respuesta**:
  ```text
  linkage-service is running
  ```

### 2. Registrar Proyecto de Vinculación
* **Ruta**: `POST /api/linkage`
* **Seguridad**: JWT requerido.
* **Cuerpo de Petición** (`application/json`):
  ```json
  {
    "name": "Alfabetización Digital Rural",
    "description": "Capacitación en computación básica a comunidades agrícolas.",
    "institution": "GAD Municipal de Calderón",
    "status": "PLANNED"
  }
  ```
* **Validaciones**:
  * `name`: No nulo ni vacío. (Retorna `400 Bad Request` en caso contrario)
  * `institution`: No nulo ni vacío. (Retorna `400 Bad Request` en caso contrario)
* **Respuestas**:
  * `200 OK`: Proyecto creado con éxito (retorna objeto con ID generado).
  * `400 Bad Request`: Parámetro requerido faltante o malformado.

### 3. Obtener Todos los Proyectos
* **Ruta**: `GET /api/linkage`
* **Seguridad**: JWT requerido.
* **Código de respuesta**: `200 OK`
* **Cuerpo de respuesta**:
  ```json
  [
    {
      "id": 1,
      "name": "Alfabetización Digital Rural",
      "description": "Capacitación en computación básica a comunidades...",
      "institution": "GAD Municipal de Calderón",
      "status": "IN_PROGRESS"
    }
  ]
  ```

### 4. Obtener Proyecto por ID
* **Ruta**: `GET /api/linkage/{id}`
* **Seguridad**: JWT requerido.
* **Respuestas**:
  * `200 OK`: Si el proyecto existe. Retorna el detalle del proyecto.
  * `404 Not Found`: Si el ID especificado no se encuentra registrado.

---

## ⚙️ Variables de Entorno (Configuración)

El servicio lee la siguiente configuración a través de variables de entorno (con fallbacks definidos para desarrollo local):

| Variable de Entorno | Valor por Defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Dirección o host de la base de datos PostgreSQL. |
| `DB_PORT` | `5432` | Puerto del servidor de base de datos PostgreSQL. |
| `DB_NAME` | `linkage_db` | Nombre de la base de datos de vinculación. |
| `DB_USER` | `postgres` | Nombre de usuario de conexión a la base de datos. |
| `DB_PASSWORD` | `postgres` | Contraseña de conexión a la base de datos. |
| `JWT_SECRET` | `v9y$B&E)H@McQfT...` | Clave secreta para la firma y validación de tokens JWT. |

---

## 🐳 Despliegue con Docker

### Construir Imagen
```bash
docker build -t gdmuzo/linkage-service:latest .
```

### Ejecutar Contenedor
```bash
docker run -d \
  --name linkage-service \
  --network pasantias-net \
  -p 8084:8084 \
  -e DB_HOST=postgres-db \
  -e DB_PORT=5432 \
  -e DB_NAME=linkage_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  gdmuzo/linkage-service:latest
```

---

## 🧪 Pruebas Unitarias e Integración
Para ejecutar las pruebas del microservicio:
```bash
./mvnw test
```
