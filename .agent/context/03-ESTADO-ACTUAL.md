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
- `hours-service` (8085): **Etapas 1, 2, 3 y 4 implementadas y validadas con tests.**
  - **Etapa 1**: Persistencia en PostgreSQL (`RegistroHoras`), operaciones `POST /api/hours` y `PATCH /api/hours/{id}/validar`.
  - **Etapa 2**: Productor Kafka que emite el evento `horas.registradas` con clave `estudianteId` y payload JSON formateado en camelCase con serialización manual.
  - **Etapa 3**: Consumidor Kafka (`@KafkaListener`), proyección de lectura en MongoDB (`horas_resumen`), recalculo de totales y endpoint de consulta `GET /api/hours/student/{estudianteId}`.
  - **Etapa 4**: Cliente gRPC (`UserServiceClient`) conectado a `user-service` para enriquecer la proyección de lectura con el `nombre` y `carrera` del estudiante de manera resiliente/best-effort.
- `user-service` (8083): scaffolding + lógica básica + `/health` (`"user-service is
  running"`). Falta: servidor gRPC (implementado en rama `feature/user-service-grpc-server`, pendiente de mergear).
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

## ✅ RESUELTO — EIP fija para el bastion de QA

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

## 🎯 Próxima tarea concreta (siguiente sesión) — ACTUALIZADO

**Fase 1 + rate limiting completos.** Único pendiente antes de continuar: hacer
`push`/deploy a `QA` y confirmar visualmente (`docker ps` en el EC2, o logs) que los 7
contenedores (`postgres-db`, `redis`, 4 microservicios, `gateway-service`,
`frontend-web`) están `Up` y que el gateway responde `429` al superar
`burstCapacity: 20` en una ráfaga de pruebas (ej. con `curl` en loop o `ab`/`hey`).

**Siguiente gran hito: `hours-service` (8085)** — primer servicio con **CQRS + Kafka**
(Fase 2). Antes de pedirle código a Antigravity, conviene definir el diseño en
`.agent/context/` (modelo de comandos, eventos, modelo de lectura) para que la
implementación sea consistente con el resto del proyecto.

> Recordatorio (sin acción aún): cuando se llegue a Fase 3 (ASG+RDS), evaluar migrar
> Redis a **ElastiCache** en PROD para que no se destruya junto con la EC2 — ver
> conversación sobre Redis/ElastiCache (pendiente de documentar en
> `02-ARQUITECTURA-TECNICA.md` cuando se aborde Fase 3).

## 🎯 Próxima tarea concreta (siguiente sesión)

1. Pedir a Antigravity el árbol completo + contenido de los controladores principales de
  `apps/gateway-service/` y `apps/linkage-service/` (ya tienen `pom.xml`, hay que saber
  cuánto está hecho).
2. Pedir el contenido de `infra/qa/main.tf` y de `infra/ansible/deploy-qa.yml` /
  `deploy-prod.yml`.
3. Decidir el punto 4 de "Decisiones pendientes" (frontend directo vs. gateway) antes de
  seguir tocando `build-args`.
4. Continuar el desarrollo de microservicios pendientes (priorizar terminar
  `gateway-service` y `linkage-service` ya que están scaffoldeados, antes de empezar
  `hours-service` desde cero).
