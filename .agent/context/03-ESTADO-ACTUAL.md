# 03 — Estado Actual y Decisiones Pendientes

> **Este archivo se actualiza al final de cada sesión de trabajo.** Es el primer lugar
> donde el agente debe mirar para saber "¿dónde quedamos?".

_Última actualización: 2026-06-12 (verificación contra REPORTE-ESTADO.md generado por Antigravity)_

## ✅ Completado / Verificado

- `gateway-service` (8082): **MUCHO más avanzado de lo esperado.** Ya tiene:
  - `JwtAuthenticationFilter` funcional (valida JWT con `jjwt`, agrega header
    `X-Auth-User`, deja pasar `OPTIONS` para CORS preflight).
  - Rutas configuradas para `auth-service` (sin JWT, correcto para login/registro),
    `internship-service`, `user-service` y `linkage-service` (con JWT).
  - Rutas de health-check con `RewritePath` hacia `/health` de `user-service` y
    `linkage-service`.
  - CORS delegado a cada microservicio (con `DedupeResponseHeader` para evitar headers
    duplicados).
  - **Pendiente**: rate limiting (requisito #5), soporte WebSocket (requisito #15), y
    agregar rutas a medida que se creen `hours-service`, `evaluation-service`, etc.

- `linkage-service` (8084): **básico ya funcional** — `LinkageController` con
  create/getAll/getById, `LinkageProject`/repository/service, y endpoint `/health`
  (`"linkage-service is running"`). Mismo nivel que `internship-service`.
- `user-service` (8083): scaffolding + lógica básica + `/health` (`"user-service is
  running"`). Falta: servidor gRPC (requisito #15).
- Monorepo gestionado con **Turbo** (`.turbo/`, `package.json` por servicio con scripts
  que envuelven `./mvnw build/test`). Esto es correcto y no debe "limpiarse".
- `user.db` / `auth.db` / `linkage.db` son los fallbacks SQLite locales documentados
  desde el inicio. **Patrón identificado**: cada nuevo servicio parece scaffoldearse
  copiando la estructura de `auth-service` como plantilla (de ahí que `linkage-service`
  tenga tanto `auth.db` como `linkage.db`). No es un error que requiera limpieza
  inmediata, pero conviene agregar `*.db` a `.gitignore` global para que estos archivos
  de plantilla no terminen versionados por accidente.

## ✅ RESUELTO — Limpieza de `frontend-web`

- `.env` con `VITE_API_URL` obsoleta eliminado.
- `Dockerfile`: quitados `ARG`/`ENV` de `VITE_AUTH_PORT`/`VITE_INTERNSHIP_PORT` (dead
  code); agregado `ARG VITE_GATEWAY_PORT` + `ENV VITE_GATEWAY_PORT=$VITE_GATEWAY_PORT`
  para que el build-arg de CI/CD llegue correctamente al build de Vite. ✅

## ✅ RESUELTO — Bug de shell en `JWT_SECRET` (Ansible)

**Encontrado en el primer deploy real end-to-end de QA** (12/jun, entrega del
microservicio adicional): `-e JWT_SECRET=<valor>` sin comillas en los `docker run` de
`auth-service` y `gateway-service` (en `deploy-qa.yml` y `deploy-prod.yml`) rompía
`/bin/sh` porque el secreto contiene `$ & ( ) ! * ?`. Corregido envolviendo el valor en
comillas simples (`-e JWT_SECRET='...'`) en las 4 ocurrencias. ✅

**Nota para el futuro**: este bug llevaba tiempo invisible porque nunca se había
ejecutado un deploy real de punta a punta. Buena señal para priorizar, en cuanto haya
margen, una corrida de `terraform plan`/deploy real de PROD también (no solo revisión
de código), para detectar este tipo de problemas antes de que aparezcan bajo presión de
tiempo.

## 🟢 Mejora futura (no urgente) — Sacar `JWT_SECRET` del código

`JWT_SECRET` sigue hardcodeado en texto plano en `.github/workflows/deploy-*.yml` y en
`infra/ansible/deploy-*.yml`. Patrón mejor (visto como referencia en el proyecto de un
compañero): guardarlo como GitHub Secret y pasarlo como extra-var de Ansible
(`-e "jwt_secret=${{ secrets.JWT_SECRET }}"`), con el playbook usando `{{ jwt_secret }}`
en el `docker run`. Misma idea aplicaría a `DB_PASSWORD`. No bloqueante para hoy.

## ✅ RESUELTO — `qa_auth_jobs` con IP pública (Cloudflare/Excel, 15/jun)

`qa_auth_jobs` movida de `private_1a` (10.0.3.0/24) a `public_1a` (10.0.1.0/24),
con EIP fija **`18.232.199.190`** y puertos 80/8082 abiertos en `sg_private`.
Nueva IP privada: `10.0.1.61` (era `10.0.3.95`). `QA_AUTH_JOBS_IP` actualizado en
GitHub Secrets. Deploy vía Ansible re-ejecutado, pipeline verde. Verificado:
- `http://18.232.199.190` → `200 OK`, frontend React cargando.
- `http://18.232.199.190:8082/api/linkage/health` → `200 OK`, `linkage-service is running`.

Excel de Cloudflare llenado:
- **QA IP1** → `18.232.199.190`
- **PRODUCCION IP** → `pasantias-prod-elb-115885246.us-east-1.elb.amazonaws.com`

Cuando la cátedra asigne los subdominios `*.distribuidauce.org`, actualizar
`01-REQUERIMIENTOS-MAESTROS.md` requisito #5 a "✅" y documentar los dominios en
el README principal y en la sección 6 del documento de entrega.

El bastion de QA (`pasantias-qa-bastion`) ahora tiene una Elastic IP fija:
**`3.225.171.116`** (igual que PROD). Se agregó `aws_eip.bastion_eip` a
`infra/qa/main.tf` (mismo patrón que PROD) + `lifecycle { ignore_changes = [ami] }` en
`aws_instance.bastion` de **ambos** `infra/qa/main.tf` e `infra/prod/main.tf` (bug
latente preexistente: sin esto, cualquier `plan`/`apply` futuro reemplazaría el bastion
por completo cada vez que Canonical publica una nueva build de la AMI Ubuntu 24.04).
`terraform apply` aplicado en QA (cuenta #2), limpio, sin destrucción de recursos.
`QA_BASTION_IP` actualizado en GitHub Secrets a `3.225.171.116` — **no debería volver a
cambiar entre sesiones de AWS Academy**.

1. ~~Verificar `QA_BASTION_IP` en GitHub Secrets~~ ✅ RESUELTO PERMANENTEMENTE (ver
  arriba) — ya no es necesario revisar esto antes de cada push.
2. **Riesgo de memoria en `qa_auth_jobs` (`t3.small`, 2GiB RAM)**: ahora corre 8
  contenedores (postgres-db, redis, 4 microservicios Java, gateway-service,
  frontend-web), 5 de ellos JVMs. Es plausible que se acerque al límite de RAM. Tras el
  próximo deploy, revisar con `docker stats` y `free -h` en el EC2. Si hay
  OOM-kills/swap, opciones: (a) subir temporalmente a `t3.medium` solo durante sesiones
  de prueba, o (b) ajustar `-Xmx` de las JVMs vía `JAVA_OPTS`/`JAVA_TOOL_OPTIONS` en los
  `docker run` de Ansible (ej. `-Xmx256m` por servicio) para acotar el consumo total.

- `RequestRateLimiter` + `KeyResolver` híbrido (JWT o IP) en rutas de
  `internship-service`, `user-service`, `linkage-service`. ✅
- Contenedor `redis:7-alpine` agregado a `infra/ansible/deploy-qa.yml` y
  `deploy-prod.yml` (red `pasantias-net`, puerto 6379, levantado antes de
  `gateway-service`), con `REDIS_HOST=redis`/`REDIS_PORT=6379` inyectadas al gateway. ✅
- Nota de estilo menor (no bloqueante): la tarea "Levantar redis" usa el literal
  `redis:7-alpine` en vez de la variable `redis_image` ya definida — funciona igual,
  cosmético.

`auth-service` e `internship-service` ya tienen `GET /health` (retornan
`"<servicio> is running"`, siguiendo el mismo patrón de `linkage-service`/
`user-service`). Los 6 target groups de `infra/prod/main.tf` ahora tienen un endpoint
de salud válido. ✅

(Nota: `*.db` ya estaba en `.gitignore` línea 16 — no era necesario agregarlo, mi
suposición anterior estaba equivocada en ese punto.)

### CI/CD (`.github/workflows/deploy-qa.yml` y `deploy-prod.yml`)

- [x] **Tests antes de build de imágenes** — RESUELTO. Corre `./mvnw test` para los 5
  servicios con `pom.xml` antes de `docker/build-push-action`.
- [x] Login a Docker Hub + build/push con tags correctos (`:qa` / `:latest`) — RESUELTO.
- [x] Deploy vía Ansible a través del bastion con inventario dinámico — RESUELTO.
  **Verificado en detalle**: los playbooks `deploy-qa.yml`/`deploy-prod.yml` levantan
  con `docker run` (no docker-compose) los 7 contenedores: `postgres-db` (con
  `init-multiple-dbs.sh` para `auth_db,internship_db,user_db,linkage_db`),
  `auth-service`, `internship-service`, `user-service`, `linkage-service`,
  `gateway-service` (con las URLs internas de los otros 4 servicios inyectadas por env
  vars) y `frontend-web`. **Esto significa que el stack completo de 4 microservicios +
  gateway + frontend ya se despliega de extremo a extremo.**
- [ ] **Build-args del frontend incompletos.** Solo se pasa `VITE_GATEWAY_PORT=8082`.
  Los `outputs` de `infra/prod/main.tf` sugieren que también se necesitan
  `VITE_AUTH_URL` / `VITE_INTERNSHIP_URL` (DNS del ELB). Con la decisión cerrada de
  "Frontend → Gateway" (ver Decisiones cerradas), esto se resuelve simplemente
  exponiendo `VITE_GATEWAY_URL` (DNS del ELB + puerto 8082) — no se necesitan URLs
  individuales por servicio.
- [ ] **No existe workflow de Terraform** (`plan`/`apply`). Los workflows actuales
  asumen que la infra ya existe. Falta crear `infra-plan-apply.yml` (manual o por
  cambios en `infra/**`) con `terraform plan` obligatorio antes de `apply`.

### Infraestructura Terraform (`infra/prod/main.tf`)

- [x] VPC propia (`10.0.0.0/16`) con subredes públicas (2 AZs: `us-east-1a`,
  `us-east-1b`) y subred privada — RESUELTO (parcial, ver issue nuevo abajo).
- [x] Bastion host con Elastic IP fija — RESUELTO.
- [x] Security groups segmentados correctamente: SG del ELB recibe tráfico público en
  los puertos de cada servicio (80, 8080-8084), y el SG privado **solo** acepta tráfico
  desde el SG del ELB o SSH desde el bastion — RESUELTO. La exposición "0.0.0.0/0" ya no
  es directa a las instancias, está bien encapsulada en el ELB.
- [x] AMI resuelta vía `data "aws_ami"` (Ubuntu 24.04, filtro por nombre, sin
  hardcodear) — RESUELTO.
- [x] `user_data` solo instala Docker, no levanta la app directamente, y tiene
  `lifecycle.ignore_changes = [user_data, ami]` — RESUELTO.
- [x] Bloques `output` presentes (`bastion_eip`, `elb_dns_name`,
  `prod_auth_jobs_private_ip`) — RESUELTO.
- [ ] **NUEVO — Falta ASG (requisito #12 "ELB-ASG")**: hay ELB con 6 listeners, pero
  los 6 target groups apuntan TODOS a la misma única instancia
  (`prod_auth_jobs`). No hay Auto Scaling Group. Single point of failure.
- [ ] **NUEVO — Falta RDS multi-AZ (requisito #18, alta disponibilidad)**: PostgreSQL
  sigue corriendo en contenedor dentro del EC2, no como RDS primary/standby.
- [ ] **NUEVO — Falta ElastiCache Redis**: Redis sigue containerizado.
- [ ] **NUEVO — Solo hay una subred privada (`us-east-1a`)**. Para multi-AZ real con
  ASG/RDS necesitaría existir también `private_1b`.
- [ ] **NUEVO — NAT Gateway está activo** (`aws_nat_gateway.nat`). Es uno de los
  recursos que más rápido consume el crédito de AWS Academy (cobra por hora aunque no
  se use). Ver `05-AWS-ACADEMY-ESTRATEGIA.md`.
- [x] Health checks verificados — ver "BUG CRÍTICO" al inicio del documento:
  `auth-service` e `internship-service` necesitan endpoint `/health` antes de aplicar
  Terraform PROD/QA.

### `infra/qa/main.tf` — revisado

- Versión "reducida" de PROD, correctamente acotada: 1 sola AZ (`us-east-1a`), sin ALB,
  sin múltiples Target Groups — solo VPC + subred pública/privada + NAT Gateway +
  bastion + 1 EC2 (`qa_auth_jobs`).
- ⚠️ **Diferencia operativa importante con PROD**: el bastion de QA usa la IP pública
  *dinámica* de la instancia (`aws_instance.bastion.public_ip`), que **cambia cada
  sesión de AWS Academy**. El propio archivo lo documenta: hay que actualizar el secret
  `QA_BASTION_IP` en GitHub Actions después de cada `terraform apply` en una sesión
  nueva. PROD, en cambio, usa una Elastic IP fija (no cambia). **Acción recomendada**:
  considerar agregar también una EIP al bastion de QA para evitar tener que actualizar
  secrets manualmente cada sesión (cuesta poco crédito y ahorra fricción operativa).
- NAT Gateway activo aquí también — mismo flag de costo que en PROD.

### `infra/ansible/` (`deploy-qa.yml`, `deploy-prod.yml`) — revisados

- Ambos playbooks son prácticamente idénticos (solo cambian tags `:qa` vs `:latest`).
- Levantan, vía `docker run` directo (no docker-compose): `postgres-db` (con
  `POSTGRES_MULTIPLE_DATABASES=auth_db,internship_db,user_db,linkage_db` e
  `init-multiple-dbs.sh`), `auth-service`, `internship-service`, `user-service`,
  `linkage-service`, `gateway-service` (con las 4 URLs internas + `JWT_SECRET`
  inyectadas como env vars) y `frontend-web`. Red Docker compartida `pasantias-net`.
- **Conclusión**: el stack completo de Fase 1 (4 microservicios + gateway + frontend +
  Postgres con 4 DBs) ya se despliega end-to-end con un solo `git push` a `QA`/`main`,
  una vez la infraestructura (bastion + EC2) existe. Falta solo el fix de `/health` para
  que el ALB de PROD lo considere saludable.

## ✅ Decisiones cerradas (ya no son "pendientes")

1. **Puertos**: `user-service`=8083, `hours-service`=8085, `gateway-service`=8082
  reservado. **CONFIRMADO** — `user-service` ya corre en 8083. ✅
2. **Frontend ↔ Gateway**: ✅ **YA IMPLEMENTADO (verificado)** — el frontend calcula
  dinámicamente `http://${window.location.hostname}:${VITE_GATEWAY_PORT || 8082}` y
  todas las llamadas (`/api/auth/**`, `/api/internships/**`, `/api/users/**`,
  `/api/linkage/**`, con header `Authorization: Bearer`) ya pasan por
  `gateway-service`. No era una tarea pendiente, ya estaba hecho. **Fase 1
  completa.**
3. **PAAS (requisito #6) = Supabase**, usado para un módulo de **"Encuestas de
  satisfacción / feedback post-práctica"**: al finalizar una pasantía, el estudiante
  califica su experiencia. Módulo aislado, no crítico (no toca notas/horas/JWT),
  demuestra integración real con un PAAS externo. Pendiente de implementación: tabla en
  Supabase + endpoint o llamada directa desde frontend.
4. **"On-premise" para backups (requisito #19) = Cuenta AWS Academy #4**, con una
  instancia `t3.micro` corriendo MinIO o un servidor SFTP, recibiendo `pg_dump`
  periódicos desde `prod_auth_jobs` vía cron + rsync/scp. Documentar explícitamente como
  simulación académica en `infra/README.md`.
5. **ASG / RDS multi-AZ / ElastiCache (requisitos #12, #18)**: se confirma posponer a
  Fase 3 (semanas 9-12). Se implementarán en Terraform de forma que puedan
  `apply`/`destroy` cerca de los checkpoints, sin correr 24/7 durante el desarrollo
  diario (ver `05-AWS-ACADEMY-ESTRATEGIA.md`).

## 🔴 Decisiones aún pendientes

- Ninguna decisión estructural pendiente por ahora.

## ✅ COMPLETADO — `hours-service` (Semana 1, CQRS + Kafka + Mongo + gRPC)

Implementado en 5 etapas sobre la rama `feature/hours-service-postgres` (PR abierto
hacia `QA`, **sin mergear** — ver nota abajo), siguiendo
`.agent/context/10-DISENO-HOURS-SERVICE.md`:

1. Esqueleto PostgreSQL (`hours_db`, entidad `RegistroHoras`, comandos REST con
   400/404 igual estilo que `linkage-service`).
2. Productor Kafka — publica `horas.registradas` tras cada `save()`.
3. Consumidor Kafka + proyección MongoDB `horas_resumen` + `GET /api/hours/student/{id}`.
4. Cliente gRPC hacia `user-service` (puerto 9083) para enriquecer
   `nombre`/`carrera` — "best effort" (`Optional.empty()` si falla, nunca bloquea).
5. Swagger (`springdoc`), README completo, y **prueba de integración end-to-end con
   Testcontainers** (Postgres + Kafka + MongoDB reales) que valida el pipeline
   completo: `POST /api/hours` → evento Kafka → proyección Mongo actualizada →
   `GET /api/hours/student/{id}` → `PATCH .../validar` → totales recalculados.

**17/17 tests pasando**, incluyendo la integración end-to-end. `hours-service` queda
funcionalmente completo y validado localmente.

### 🔧 Lección para futuros servicios con MongoDB (`document-service`, `report-service`)
**Spring Boot 4.x renombró el prefijo de propiedad de MongoDB**: usar
`spring.mongodb.uri` (NO `spring.data.mongodb.uri`, que es el nombre de Spring Boot
3.x y versiones anteriores — verificado independientemente, es un cambio real y
documentado de `spring-boot-starter-data-mongodb` 4.x). Aplicar desde el inicio en
Semana 3.

### ✅ MongoDB Atlas configurado (13/jun)
Cuenta gratuita (M0) creada, cluster `Cluster0` en AWS us-east-1, usuario de BD con
permisos read/write, Network Access `0.0.0.0/0` (correcto para free tier — la
protección real es usuario/contraseña, no IP allowlist). Connection string obtenido
y **NO almacenado en ningún archivo del repo ni de `.agent/`** — se agregará como
GitHub Secret `MONGO_URI` en Semana 4 (mismo patrón que `JWT_SECRET`/`DB_PASSWORD`),
con el path `?...&` + nombre de BD ajustado por servicio (`hours_read_db`,
`documents_db`, `reports_db`). Recordatorio: usar `spring.mongodb.uri` (ver lección
arriba), no `spring.data.mongodb.uri`.

## ✅ COMPLETADO — `evaluation-service` (Semana 2, parcial)

`apps/evaluation-service` (8086, Layered + PostgreSQL `evaluation_db` + gRPC client
hacia `user-service`). Reutilizó `linkage-service` como plantilla y **copió el
cliente gRPC completo de `hours-service`** (`user.proto`, `UserServiceClient`,
`UserServiceClientImpl`, `StudentInfo`) — sin "descubrimientos" nuevos de Spring Boot
4, mucho más rápido que `hours-service`. Endpoints: `POST /api/evaluations` (valida
`estudianteId`/`tutorId`/`calificacion`, incluyendo rango 0-10 → 400),
`GET /api/evaluations/{id}` (404 si no existe), `GET /api/evaluations/student/{id}`
(enriquecido con nombre/carrera vía gRPC, best-effort). **12/12 tests, BUILD
SUCCESS**. Rama `feature/evaluation-service`, PR hacia `QA` — confirmar que se abrió
(último paso pedido a Antigravity, sin confirmación de link todavía).

`notification-service` (8087, Event-Driven + Kafka consumer + MQTT) queda **completado localmente** (verificación de tests unitarios e integración con Testcontainers exitosa). Consume eventos de `horas.registradas` con estado `VALIDADO` o `RECHAZADO`, los guarda en PostgreSQL (`notification_db`) y publica un JSON a MQTT en el topic dinámico `notificaciones/{estudianteId}` usando cifrado TLS hacia HiveMQ Cloud. El backend de pruebas pasa al 100%.

`document-service` (8088, Event-Driven + PDF Generation + REST + S3 + Webhooks) queda **completado localmente** (verificación de 8 tests unitarios exitosa). Consume eventos de `horas.registradas` con estado `VALIDADO`, genera un archivo PDF en memoria utilizando OpenPDF (iText 2.1.7), lo sube a Amazon S3 (`pasantias-documents-qa` con tokens temporales de AWS Academy), guarda la metadata en PostgreSQL (`document_db`), realiza un upsert en MongoDB (`documentos_resumen`) y dispara un webhook de forma asíncrona ("best effort") a n8n.

### Estado de PRs (NO mergear todavía — plan para mañana abajo)
- `feature/user-service-grpc-server` → `QA`: agrega servidor gRPC
  `GetStudentInfo` (puerto 9083) + columna `carrera` (ddl-auto=update, sin
  migración manual necesaria).
- `feature/hours-service-postgres` → `QA`: las 5 etapas de `hours-service`.
- `feature/evaluation-service` → `QA`: `evaluation-service` completo.
- `feature/document-service` → `QA` (o rama local `feature/document-service` pendiente de push): `document-service` completo y verificado localmente.

## 🚨 URGENTE (prioridad #1, antes que lo demás) — Dominio Cloudflare

El docente exigió (WhatsApp, 14/jun, con amenaza explícita de bajar nota) implementar
el **dominio Cloudflare delante del ALB de PROD** — requisito #5 de
`01-REQUERIMIENTOS-MAESTROS.md`, marcado "🔲 Pendiente", **nunca implementado**.
Gestionado por la cátedra (dominio `distribuidauce.org`) vía un Excel donde cada
estudiante aporta 2 "orígenes" (PROD y QA) y la cátedra asigna 2 subdominios de
vuelta (ej. visto en la fila de un compañero:
`uce-prod-elb-....amazonaws.com` → `<alumno>pro1.distribuidauce.org`,
`<IP pública QA>` → `<alumno>pro2.distribuidauce.org`).

### PROD — listo para llenar YA (sin trabajo nuevo)
Origen PROD = `pasantias-prod-elb-115885246.us-east-1.elb.amazonaws.com`.

### QA — requiere una EIP nueva en `qa_auth_jobs` (próxima sesión, ~mismo esfuerzo
que la EIP del bastion de ayer)
`qa_auth_jobs` (10.0.3.95) es privada hoy. Plan:
1. Agregar `aws_eip` a `qa_auth_jobs` (no al bastion — ya tiene la suya).
2. En `sg_private`, agregar ingress **solo puertos 80 (frontend) y 8082 (gateway)**
   desde `0.0.0.0/0`. **NO tocar la regla de SSH (22)** — sigue restringida al
   bastion (`sg_bastion`), eso se mantiene como fortaleza documentada.
3. `terraform apply` en cuenta QA (#2) — requiere "Start Lab".
4. Verificar desde fuera (sin túnel): `http://<nueva_EIP_qa>` (frontend) y
   `http://<nueva_EIP_qa>:8082/api/linkage/health` (gateway).
5. Esa IP es el "origen QA" del Excel.

### Después de llenar el Excel
Cuando la cátedra asigne los subdominios `*.distribuidauce.org`: actualizar
`01-REQUERIMIENTOS-MAESTROS.md` (#5) a "✅", y agregar ambos dominios a la sección 6
del documento de entrega de `linkage-service` (probar también vía el dominio nuevo,
no solo el DNS crudo del ALB).

### Acción adicional rápida (5 min, SIN AWS — se puede hacer en cualquier momento)
Editar la descripción del PR #25 (`QA → master`, ya mergeado) en GitHub para
referenciar explícitamente el PR #20 (`feature → QA`) y los runs de Actions #37/#38
como evidencia de que se probó en QA antes de ir a `master` — el docente señaló que
"nadie adjuntó la evidencia para ir a prod".

## 🎯 Próxima tarea concreta (siguiente sesión) — Plan de merge a QA

**Objetivo de la sesión**: mergear los 3 PRs pendientes a `QA` y validar con GitHub
Actions, en este orden (NO mergear los 3 a la vez):

1. **"Start Lab"** en la cuenta QA (#2), esperar a que las instancias estén
   `running`. La EIP del bastion (`3.225.171.116`) y la IP privada de `qa_auth_jobs`
   (`10.0.3.95`) no deberían cambiar (stop/start preserva IPs, a diferencia de
   terminar/recrear) — no debería requerir actualizar secrets de GitHub.
2. **Mergear PRIMERO solo `feature/user-service-grpc-server` → QA**, sola. Es el
   único de los 3 que afecta el deploy actual (`user-service` está en la lista de
   build/Ansible de `deploy-qa.yml`; los otros dos no están referenciados todavía).
   Esto dispara `deploy-qa.yml`: rebuild + redeploy de `user-service` con el gRPC
   server nuevo + columna `carrera`.
3. **Verificar**: run de Actions en verde; `docker ps` muestra `user-service` con
   imagen nueva (`Up` reciente); `docker logs user-service` muestra el gRPC server
   arrancando en el puerto 9083; (opcional) confirmar columna `carrera` en la tabla
   vía `docker exec postgres-db psql ...`.
4. **Solo si 2-3 salen bien**: mergear `feature/hours-service-postgres` y
   `feature/evaluation-service` → QA (deberían ser inertes para el deploy actual —
   sirve para confirmar que no rompen el pipeline existente antes de Semana 4).

Si algo falla en el paso 2-3, queda aislado — se sabe exactamente qué lo causó, sin
ruido de las otras dos PRs.

Después de esto (si hay tiempo): continuar con `notification-service` (Semana 2,
diseño por etapas).
