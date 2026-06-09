# API Gateway Service (`gateway-service`) 🚦

The **API Gateway** acts as the single entry point for all API requests coming from client applications (such as the frontend). It handles request routing, applies global CORS filters, and secures downstream endpoints using JWT authorization.

---

## 🚀 Key Features

* **Centralized Routing**: Forwards requests dynamically to the corresponding microservices.
* **Token Validation**: Intercepts requests to protected resources and authenticates the user via the `JwtAuthenticationFilter`.
* **CORS Management**: Configured globally to allow cross-origin requests from the React frontend.
* **Health Checks routing**: Exposes special paths for checking downstream microservice health.

---

## 🛠️ Technology Stack

* **Java 17**
* **Spring Boot 4.0.6**
* **Spring Cloud Gateway (WebFlux)**
* **JSON Web Token (jjwt 0.13.0)** (for parsing and validating JWT signatures)

---

## 📦 Main Directory Structure

```text
apps/gateway-service/
├── src/main/java/com/uce/gateway_service/
│   ├── GatewayServiceApplication.java   # App entry point
│   └── JwtAuthenticationFilter.java     # Custom Gateway filter that enforces JWT validation
├── src/main/resources/
│   └── application.yml                  # Config file containing route path definitions
├── Dockerfile                           # Containerization instructions
└── pom.xml                              # Maven dependency definitions
```

---

## ⚙️ Configuration and Routes

The service is configured in `src/main/resources/application.yml`. 

### Key Mappings

The gateway listens on port **`8082`** and forwards routes based on matching path prefixes:

| Path Prefix | Destination Service | Port | Security Filter |
|---|---|---|---|
| `/api/auth/**` | `auth-service` | `8080` | Public (Unsecured) |
| `/api/internships/**` | `internship-service` | `8081` | Enforced JWT Filter |
| `/api/users/health` | `user-service` (`/health`) | `8083` | Public (Unsecured) |
| `/api/users/**` | `user-service` | `8083` | Enforced JWT Filter |
| `/api/linkage/health` | `linkage-service` (`/health`) | `8084` | Public (Unsecured) |
| `/api/linkage/**` | `linkage-service` | `8084` | Enforced JWT Filter |

---

## 🔒 Security Filter details

The `JwtAuthenticationFilter` checks for the presence of a valid JSON Web Token in the `Authorization` header of incoming requests:

* **Header Format**: `Authorization: Bearer <JWT_TOKEN>`
* If the token is missing, invalid, or expired, the gateway responds with `401 Unauthorized` directly, without forwarding the request to internal microservices.

---

## 🐳 Running inside Docker

### Build Image
```bash
docker build -t gdmuzo/gateway-service:latest .
```

### Run Container
To run the container, make sure it is attached to the same network as your other services:
```bash
docker run -d \
  --name gateway-service \
  --network pasantias-net \
  -p 8082:8082 \
  -e AUTH_SERVICE_URL=http://auth-service:8080 \
  -e INTERNSHIP_SERVICE_URL=http://internship-service:8081 \
  -e USER_SERVICE_URL=http://user-service:8083 \
  -e LINKAGE_SERVICE_URL=http://linkage-service:8084 \
  -e JWT_SECRET=v9y$B&E)H@McQfTjWmZq4t7w!z%C*F-JaNdRgUkXp2r5u8x/A?D(G+KbPeShVmYq \
  gdmuzo/gateway-service:latest
```
