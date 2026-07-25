# Resumen de Vulnerabilidades (OWASP Dependency-Check)

## Tabla Resumen Global
| Servicio | Deps | Críticas | Altas | Medias | Bajas | Total CVE |
|----------|------|----------|-------|--------|-------|-----------|
| auth-service | 96 | 8 | 15 | 39 | 1 | 87 |
| gateway-service | 120 | 21 | 98 | 67 | 0 | 186 |
| internship-service | 133 | 22 | 79 | 82 | 1 | 208 |
| document-service | 125 | 13 | 50 | 71 | 1 | 159 |
| ai-service | No aplica | No aplica | No aplica | No aplica | No aplica | No aplica* |

*\*El análisis de dependencias Python del ai-service requiere una herramienta especializada (pip-audit / Safety); OWASP Dependency-Check tiene soporte limitado para este ecosistema.*

## Top 5 CVE más críticos
| ID CVE | Dependencia | Servicio | Score CVSS | Descripción breve |
|--------|-------------|----------|------------|-------------------|
| CVE-2026-45674 | gateway-service-0.0.1-SNAPSHOT.jar: netty-transport-4.2.12.Final.jar | gateway-service | 10.0 | Netty is a network application framework for development of protocol servers and clients. Prior to versions 4.1.135.Final and 4.2.15.Final, Netty's Dn... |
| CVE-2026-47691 | gateway-service-0.0.1-SNAPSHOT.jar: netty-transport-4.2.12.Final.jar | gateway-service | 10.0 | Netty is a network application framework for development of protocol servers and clients. Prior to versions 4.1.135.Final and 4.2.15.Final, Netty's `D... |
| CVE-2026-45674 | gateway-service-0.0.1-SNAPSHOT.jar: reactor-netty-core-1.3.5.jar | gateway-service | 10.0 | Netty is a network application framework for development of protocol servers and clients. Prior to versions 4.1.135.Final and 4.2.15.Final, Netty's Dn... |
| CVE-2026-47691 | gateway-service-0.0.1-SNAPSHOT.jar: reactor-netty-core-1.3.5.jar | gateway-service | 10.0 | Netty is a network application framework for development of protocol servers and clients. Prior to versions 4.1.135.Final and 4.2.15.Final, Netty's `D... |
| CVE-2026-45674 | gateway-service-0.0.1-SNAPSHOT.jar: spring-boot-netty-4.0.6.jar | gateway-service | 10.0 | Netty is a network application framework for development of protocol servers and clients. Prior to versions 4.1.135.Final and 4.2.15.Final, Netty's Dn... |