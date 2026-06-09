# Outreach Projects Service (`linkage-service`) 🔗

The **Outreach Projects Service** (Vinculación) is a Spring Boot microservice that manages university social outreach projects, tracking which external institutions are collaborating, the projects' statuses, and descriptions.

---

## 🚀 Key Features

* **Create Projects**: Allows coordinators to register outreach projects.
* **List Projects**: Shows all social projects currently planned, in progress, or completed.
* **Health Endpoint**: Native `/health` check routed through the gateway to monitor operational state.
* **SQLite Storage**: Persistent lightweight relational database (`linkage.db`).

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Data JPA**
* **SQLite JDBC & Hibernate Dialect**

---

## 📦 Main Directory Structure

```text
apps/linkage-service/
├── src/main/java/com/uce/linkage_service/
│   ├── LinkageServiceApplication.java   # App bootstrap & health endpoint
│   ├── LinkageController.java          # REST handlers for outreach projects
│   ├── models/                          # Data model representing a project
│   ├── repositories/                    # Spring Data repository interface
│   └── services/                        # Service layer handling project creation and fetch
├── Dockerfile                           # Container configuration
└── pom.xml                              # Build configuration
```

---

## ⚙️ Configuration Properties

Key settings in `src/main/resources/application.properties`:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8084` | Port on which the service runs. |
| `spring.datasource.url` | `jdbc:sqlite:linkage.db` | Location of the SQLite database. |

---

## 🔌 API Endpoints

Endpoints are protected and must be accessed via the API Gateway.

### 1. Health Status
* **Endpoint**: `GET /api/linkage/health` (Exposed publicly via rewrite in the gateway)
* **Response**:
  ```text
  "linkage-service is running"
  ```

### 2. Retrieve All Projects
* **Endpoint**: `GET /api/linkage`
* **Security**: Enforced JWT authorization header.
* **Response**: List of projects.
  ```json
  [
    {
      "id": 1,
      "name": "Rural Digital Literacy",
      "description": "Providing basic computing training to local agricultural communities.",
      "institution": "Calderon Municipality GAD",
      "status": "IN_PROGRESS"
    }
  ]
  ```

### 3. Create Project
* **Endpoint**: `POST /api/linkage`
* **Security**: Enforced JWT authorization header.
* **Body** (`application/json`):
  ```json
  {
    "name": "Rural Digital Literacy",
    "description": "Providing basic computing training to local agricultural communities.",
    "institution": "Calderon Municipality GAD",
    "status": "PLANNED"
  }
  ```
* **Response**: The created project metadata.

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/linkage-service:latest .
```

### Run Container
```bash
docker run -d \
  --name linkage-service \
  --network pasantias-net \
  -p 8084:8084 \
  -v /var/lib/pasantias/linkage.db:/app/linkage.db \
  gdmuzo/linkage-service:latest
```
