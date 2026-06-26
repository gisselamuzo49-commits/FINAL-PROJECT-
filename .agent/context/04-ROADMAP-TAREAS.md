# 04 — Roadmap del Proyecto (convertido en tareas)

Basado en las fases del documento de propuesta, ajustado con los hallazgos de la tabla de
requerimientos. Cada tarea marcada `[ ]` debe pasar a `[x]` y reflejarse también en
`03-ESTADO-ACTUAL.md` cuando se complete. La Fase 1 (Semanas 1-4 del plan original) está
completa; las Fases 2-3 se detallan abajo como un plan de 6 semanas (acordado 13-14/jun),
que reemplaza la planificación genérica original.

## Fase 1 — Servicios Core y Autenticación (Semanas 1-4)

- [x] `auth-service` (8080) — JWT, RBAC, hashing.
- [x] `internship-service` (8081) — CRUD de ofertas.
- [x] `user-service` (8083) — perfiles básicos e implementado endpoint de búsqueda por email (`/email/{email}`). Falta servidor gRPC.
- [x] `linkage-service` (8084) — CRUD básico + `/health` implementados.
- [x] `gateway-service` (8082) — rutas + JWT funcionando para los 4 servicios. Falta:
  rate limiting, WebSocket, rutas de servicios futuros.
- [x] Configurar 4 bases PostgreSQL lógicas (`auth_db`, `internship_db`, `user_db`,
  `linkage_db`) — verificado en `infra/ansible/*.yml` vía `init-multiple-dbs.sh`.
- [x] Frontend: flujos de login/registro + paneles para los 4 servicios + páginas de Horas (`Hours.jsx`), Evaluaciones (`Evaluations.jsx`), Documentos (`Documents.jsx`), Notificaciones (`Notifications.jsx`) y Recomendaciones (`Recommendations.jsx`) — verificado
  (`LoginCard`, `InternshipsTab`, `ProfilesTab`, `LinkageTab`, todo vía gateway con JWT).
- [x] Swagger/OpenAPI habilitado y documentado en cada servicio (requisito #21). ✅ Completado en rama `feature/swagger-openapi` — todos los 11 microservicios tienen Swagger habilitado.
- [ ] Conventional commits + plantilla de PR aplicados desde ahora (requisito #21).

> ✅ **Fase 1 completa** (excepto Swagger/conventions, transversales y de bajo riesgo —
> pueden hacerse en paralelo con la Fase 2).

## Próximas 6 Semanas — Plan Detallado (reemplaza/detalla Fases 2 y 3)

> Acordado en sesión del 13-14/jun. Cada semana usa AWS solo cuando es necesario — ver
> `05-AWS-ACADEMY-ESTRATEGIA.md` para qué cuenta usar y cuándo destruir recursos.
> Referencias a "09-ADOPCIONES" = `.agent/context/09-ADOPCIONES-OTRO-PROYECTO.md`
> (uso interno).

### Semana 1 — Fundación de mensajería + `hours-service` (CQRS)
- [ ] Kafka + RabbitMQ + MongoDB en `docker-compose` local (sin AWS).
- [x] MongoDB Atlas (cuenta gratuita) configurado — usado por `hours-service` (esta
  semana), `document-service` y `report-service` (semana 3). ✅ Cluster creado
  13/jun, connection string pendiente de agregar como `MONGO_URI` (GitHub Secret) en
  Semana 4.
- [x] `hours-service` (8085): CQRS — comandos REST escriben en `hours_db` (PostgreSQL),
  evento `horas.registradas` a Kafka, proyección de lectura en MongoDB. ✅ Completo
  (5 etapas + Circuit Breaker de Resilience4j programático en cliente gRPC, 19/19 tests incl. integración end-to-end con Testcontainers). PR abierto
  hacia `QA`, sin mergear — ver `03-ESTADO-ACTUAL.md`.
- [ ] Quick wins en paralelo (bajo costo, alto impacto en backlog docente):
  - [ ] Logging estructurado (niveles INFO/DEBUG/WARN/ERROR) en los 5 servicios ya
    desplegados — backlog docente #1.
  - [ ] Workspace de Postman compartido con el docente, todas las rutas actuales —
    backlog docente #2 (parcial).
  - [x] Limpieza de disco en `infra/ansible/deploy-*.yml` (`docker system prune`,
    `apt-get clean`, truncar logs >10MB, `journalctl --vacuum-size=50M`) — ver
    09-ADOPCIONES #12.
  - [ ] `terraform fmt -check -recursive` + `validate` como step de CI, sin `apply` —
    ver 09-ADOPCIONES #1.

### Semana 2 — Consumidores de eventos
- [x] `notification-service` (8087): consumidor Kafka `horas.registradas` → MQTT
  Mosquitto (topic `notificaciones/{estudianteId}`) + PostgreSQL `notification_db`.
  Publicación MQTT protegida con Circuit Breaker programático de Resilience4j.
  12/12 tests. PR abierto hacia QA, sin mergear — espera Semana 4.
- [x] `evaluation-service` (8086): Layered + PostgreSQL `evaluation_db` + cliente gRPC
  hacia `user-service` (puerto 9083, best-effort) con Circuit Breaker programático de Resilience4j.
  Validación calificación 0-10. 14/14 tests. PR abierto hacia QA, sin mergear — espera Semana 4.

### Semana 3 — Servicios periféricos restantes
- [x] `document-service` (8088): consumidor Kafka + Webhook hacia n8n + Mongo/S3 (Completado y protegido con Circuit Breaker programático de Resilience4j para subidas a S3, 10/10 tests passing).
- [x] S3 configurado para CVs y documentos generados (usado por `document-service`) — Bucket `pasantias-documents-qa` en us-east-1 creado y versionado activado.
- [ ] Configurar n8n (self-hosted en cuenta DEV/Sandbox #1 o local) y conectar al menos
  un flujo real (ej. notificación por correo cuando se aprueba una práctica).
- [x] `report-service` (8089): consumidor Kafka + endpoint SOAP + Mongo (Completado y protegido con Circuit Breaker programático de Resilience4j para llamadas REST a `document-service`, 13/13 tests passing).
- [x] Swagger/OpenAPI (`springdoc-openapi-starter-webmvc-ui`) en los 11 microservicios. ✅ Completado — `auth-service`, `internship-service`, `user-service`, `linkage-service`, `report-service` añadidos; `ai-service` (FastAPI `/docs`) actualizado con título y descripción.
- [ ] PAAS secundario **Supabase** — módulo "Encuestas de satisfacción / feedback
  post-práctica" (tabla en Supabase + integración desde frontend, decisión cerrada en
  `03-ESTADO-ACTUAL.md`). Sin dependencias de otros servicios, puede hacerse cualquier
  semana si sobra tiempo — colocado aquí por defecto.

### Semana 4 — IA + primer deploy completo a QA
- [ ] `ai-service` (8090, FastAPI): spaCy, TF-IDF + similitud de coseno, Random Forest
  de riesgo de deserción, cola RabbitMQ (contenedor con healthcheck — ver
  09-ADOPCIONES #4).
- [x] `gateway-service`: rutas para los 6 servicios nuevos (URLs añadidas en deploy-qa.yml) + soporte WebSocket. ✅
- [x] Corregir bugs críticos en `infra/ansible/deploy-qa.yml` (Mosquitto autotenticado, mapeo de URLs en gateway, tags :qa en servicios). ✅
- [x] Deploy completo a QA (18 contenedores: microservicios + Kafka + RabbitMQ + MongoDB + Redis + Postgres). Instancia configurada como `t3.large` (8 GiB RAM) + Swap de 4GB + límites de JVM establecidos. ✅
- [x] Configurar Self-Hosted Runner en el Bastion de QA y parametrizar dinámicamente IPs (Lab 53). ✅


### Semana 5 — Sesión de diseño + Terraform modular + infraestructura PROD
Sesión de diseño dedicada **antes** de tocar Terraform de PROD — ver decisiones
pendientes en 09-ADOPCIONES:
- [ ] Decidir: reducir target groups del ALB de PROD de 6 a 2 (`frontend_tg`,
  `gateway_tg`) — mejora requisito #5 (superficie de ataque) y simplifica ASG.
- [ ] Agregar bloque ASG + launch template + políticas CloudWatch — versión
  simplificada (no requiere rediseñar el deploy actual), ver 09-ADOPCIONES sección
  ASG.
- [ ] Modularizar Terraform: `infra/modules/{vpc,security_groups,ec2}` +
  `infra/{qa,prod}/main.tf` que solo instancian — blueprint en 09-ADOPCIONES #6.
- [ ] Migrar PostgreSQL → RDS (primary/standby multi-AZ, requisito #18, solo PROD,
  usa subred `private_1b`).
- [ ] Redis → ElastiCache (solo PROD).
- [ ] Volumen EBS persistente para `postgres-db`/Docker — 09-ADOPCIONES #8.
- [ ] Aplicar a PROD (cuenta #3) solo después de validar en cuenta DEV/Sandbox (#1).

### Semana 6 — Monitoreo, backups, blue-green y cierre
- [ ] Prometheus + Grafana en PROD — requisito #17.
- [ ] Job de backup `pg_dump` → cuenta #4 (on-premise simulado, decisión ya cerrada).
- [ ] Blue-green deployment vía GitHub Actions — backlog docente #4 (instancia nueva →
  health check → swap → eliminar anterior si exitoso).
- [ ] Video final de evidencia, ampliando el guion del 12/jun para cubrir todo lo
  nuevo (logs por servicio, terraform output, Kafka/RabbitMQ, diagrama event-driven
  justificado).
- [ ] Revisión final completa de `01-REQUERIMIENTOS-MAESTROS.md`.
- [ ] (Opcional) Evaluar Kubernetes solo si hay tiempo/crédito sobrante — cuenta #8.

## Hitos de checkpoint sugeridos

| Cuándo | Checkpoint |
|---|---|
| Fin Semana 1 | `hours-service` funcionando local con CQRS+Kafka demostrable (comando → evento → proyección de lectura). |
| Fin Semana 4 | Demo: los 11 microservicios corriendo en Docker Compose local + deploy completo a QA, flujo de horas con CQRS+Kafka, notificaciones en tiempo real (WebSocket+MQTT), recomendación de IA end-to-end. |
| Fin Semana 6 | Demo final: todo desplegado en AWS (QA y PROD), ASG+RDS+ElastiCache+EBS en PROD, monitoreo en vivo, CI/CD verde de extremo a extremo, blue-green demostrado, checklist de rúbrica al 100%. |
