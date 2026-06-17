# Report Service

Microservicio desarrollado con **Spring Boot 4.0.6** en el puerto `8089` encargado del procesamiento de estadísticas, acumulación de horas registradas, almacenamiento en MongoDB/PostgreSQL y exposición de endpoints de consulta vía REST y SOAP.

---

## Características Arquitectónicas

- **Consumidor Kafka**: Escucha eventos en el topic `horas.registradas` (tanto `VALIDADO` como `PENDIENTE`) para alimentar estadísticas.
- **Base de Datos Híbrida**: 
  - **PostgreSQL (`report_db`)**: Almacena registros detallados de horas por estudiante y el resumen global por estudiante (`ReporteEstudiante`).
  - **MongoDB (`report_read_db`)**: Mantiene la proyección optimizada de lectura agregada (`ReporteGlobal`).
- **Comunicación SOAP**: Expone un endpoint SOAP WSDL en `/ws/reports.wsdl` con puerto `/ws` para consultas institucionales legadas.
- **Timeout en Integración**: Posee timeouts explícitos de comunicación con `document-service` (2s de conexión, 3s de lectura).

---

## Configuración y Variables de Entorno

El servicio utiliza las siguientes variables de entorno para su despliegue y desarrollo local:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8089` | Puerto de escucha del servicio |
| `DB_HOST` | `localhost` | Host de la base de datos PostgreSQL |
| `DB_PORT` | `5432` | Puerto de la base de datos PostgreSQL |
| `DB_NAME` | `report_db` | Nombre de la base de datos PostgreSQL |
| `DB_USER` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `postgres` | Contraseña de PostgreSQL |
| `MONGO_URI` | `mongodb://localhost:27017/report_read_db` | URI de conexión a MongoDB |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Direcciones de los brokers de Kafka |
| `DOCUMENT_SERVICE_URL` | `http://localhost:8088` | URL base de integración REST con document-service |

---

## Endpoints Expuestos

### REST API
- `GET /health`: Estado de salud simple del servicio (`200 OK`).
- `GET /api/reports/student/{estudianteId}`: Obtiene el resumen del reporte del estudiante desde PostgreSQL. Retorna `404` si no existe.
- `GET /api/reports/global`: Obtiene el reporte global acumulado desde MongoDB. Retorna `404` si no existe.
- `/swagger-ui.html`: Interfaz visual de documentación Swagger de los endpoints REST.

### SOAP Web Services
El WSDL autogenerado por Spring WS está disponible en:
- `GET http://localhost:8089/ws/reports.wsdl`

La dirección de envío (POST) de las peticiones XML es:
- `POST http://localhost:8089/ws`

#### Estructura del XML SOAP de Petición (`GetReporteEstudianteRequest`):
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ws="http://uce.com/report_service/ws">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:GetReporteEstudianteRequest>
         <ws:estudianteId>student_42</ws:estudianteId>
      </ws:GetReporteEstudianteRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

#### Estructura del XML SOAP de Respuesta (`GetReporteEstudianteResponse`):
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:GetReporteEstudianteResponse xmlns:ns2="http://uce.com/report_service/ws">
         <ns2:estudianteId>student_42</ns2:estudianteId>
         <ns2:totalHorasValidadas>20.50</ns2:totalHorasValidadas>
         <ns2:totalHorasPendientes>10.00</ns2:totalHorasPendientes>
         <ns2:totalDocumentos>2</ns2:totalDocumentos>
         <ns2:ultimaActualizacion>2026-06-17T11:45:00</ns2:ultimaActualizacion>
      </ns2:GetReporteEstudianteResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
