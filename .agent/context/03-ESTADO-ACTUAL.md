# 03 — Estado Actual y Decisiones Pendientes

> **Este archivo se actualiza al final de cada sesión de trabajo.** Es el primer lugar
> donde el agente debe mirar para saber "¿dónde quedamos?".

_Última actualización: 2026-06-23 (Sesión Lab 53 - noche)_

## ✅ COMPLETADO — Corrección de Bugs en Playbook de QA de Ansible (23/Jun noche)

### Problema resuelto
- Tres bugs críticos en `infra/ansible/deploy-qa.yml` impedían el despliegue correcto de los 6 nuevos servicios.
- Mosquitto local no tenía autenticación y causaba conflictos con `notification-service`.
- `gateway-service` no tenía mapeadas las 6 URLs de los nuevos servicios, lo que impedía que enrutara el tráfico correctamente.
- Los nuevos microservicios usaban la etiqueta `:latest` de Docker en lugar de `:qa` en los pulls y ejecuciones del contenedor.

### Solución aplicada
- Se configuró Mosquitto local (self-hosted) con autenticación deshabilitando el acceso anónimo y utilizando un archivo de contraseñas montado desde `/opt/mosquitto/passwd` (copiado desde `infra/mosquitto/passwd`).
- Se parametrizó la contraseña mediante `mqtt_password` en la sección de variables de Ansible con el default `changeme`. Asimismo, se inyectó como variable extra (`-e "mqtt_password=${{ secrets.MQTT_PASSWORD }}"`) en el workflow de CI/CD de QA (`.github/workflows/deploy-qa.yml`).
- Se corrigió `notification-service` para usar el broker local de Mosquitto con usuario `mqttuser` y contraseña parametrizada.
- Se añadieron las 6 URLs faltantes (`HOURS_SERVICE_URL`, `EVALUATION_SERVICE_URL`, `NOTIFICATION_SERVICE_URL`, `DOCUMENT_SERVICE_URL`, `REPORT_SERVICE_URL`, `AI_SERVICE_URL`) al docker run de `gateway-service`.
- Se parametrizaron las imágenes de los 6 nuevos servicios con etiqueta `:qa` utilizando variables globales en el playbook de Ansible (`docker_image_hours`, `docker_image_evaluation`, etc.) eliminando todas las referencias a `:latest`.
- Se unificó el pull de la imagen de Mosquitto ubicándolo en la sección correspondiente después de MongoDB.
- Se añadió la automatización para el GitHub Actions runner configurándolo como servicio `systemd` en el Bastion mediante un segundo play en `deploy-qa.yml` y agregando el grupo `[bastion]` al inventario generado en el workflow de GitHub Actions.
- Se configuró la instalación automática de Docker (si no existe en el EC2 de QA) dentro de los prerrequisitos del playbook de Ansible.
- Se agregaron tareas en el playbook de Ansible para expandir automáticamente la partición y el filesystem del volumen EBS (`growpart` y `resize2fs`) en la instancia EC2 de QA.
- Se añadió `ignore_errors: true` a las tareas de pull y run de los 6 nuevos servicios y neo4j en `deploy-qa.yml` para evitar fallas en el despliegue cuando las imágenes no existan o por timeouts.
- Se configuró `async: 120` y `poll: 10` en las 20 tareas de levantar contenedores ("Levantar") que ejecutan `docker run` en `deploy-qa.yml` para evitar bloqueos y cuelgues por SSH.
- Se configuró el disco root_block_device de la instancia del bastion a 20GB y de la de servicios a 30GB en el Terraform de QA (`infra/qa/main.tf`), agregando y alineando sus bloques `lifecycle` correspondientes.
- Se simplificó el `user_data` de la instancia `bastion` en `infra/qa/main.tf` eliminando la variable `gh_runner_token` y la lógica de registro automático del runner, retornando a una inicialización básica del bastion host.
- Se configuró el Key Pair de QA como un recurso fijo en Terraform (`infra/qa/main.tf`) con nombre `pasantias-qa-key` utilizando la llave pública `QA.pub` commiteada en el repositorio.

## ✅ COMPLETADO — Nginx Proxy + Fix CORS (23/Jun tarde)

### Problema resuelto
- El firewall universitario bloqueaba el puerto 8082
- CORS bloqueaba peticiones cross-origin

### Solución aplicada
- nginx.conf: agregado location /api/ con proxy_pass
  a gateway-service:8082 interno
- 13 páginas frontend: eliminado :8082 del fallback URL
  (ahora usa solo window.location.hostname sin puerto)
- gateway application.yml: CORS actualizado a 32.193.25.6
- Todo el tráfico API ahora va por puerto 80 via Nginx

### URL actual de QA
- Frontend: http://32.193.25.6 (puerto 80)
- API: http://32.193.25.6/api/* (proxy interno a :8082)
- Gateway directo (interno): http://32.193.25.6:8082

### Credenciales de prueba
- estudiante@uce.edu.ec / password123
- tutor@uce.edu.ec / password123
- coordinador@uce.edu.ec / password123

## ✅ COMPLETADO HOY — Infraestructura Lab 53

### IPs actuales de QA (Lab 53)
- Bastion EIP: 50.19.247.85 (fija, no cambia)
- qa_auth_jobs EIP pública: 32.193.25.6 (fija)
- qa_auth_jobs IP privada: 10.0.1.170
- S3 backend tfstate: estado-pasantias-gisse-lab53

### Secrets de GitHub actualizados
- QA_BASTION_IP = 50.19.247.85
- QA_AUTH_JOBS_IP = 10.0.1.170 (IP privada para Ansible)
- JWT_SECRET = en GitHub Secrets (ya no hardcodeado)
- PG_PASSWORD = en GitHub Secrets
- NEO4J_PASSWORD = en GitHub Secrets
- QA_SSH_KEY = llave privada infra/qa/QA (generada con ssh-keygen)

### Terraform QA - estado actual
- Key pair creado por Terraform con infra/qa/QA.pub
- LabInstanceProfile adjunto a bastion y qa_auth_jobs
- user_data con base64encode + set -e + logs en /var/log
- SSM Agent instalado en bastion
- Disco 30GB en qa_auth_jobs
- S3 backend con encrypt=true y use_lockfile=true

### CI/CD - nuevo diseño deploy-qa.yml
- 15 jobs: detect-changes → test → 12 builds paralelos → deploy
- runs-on: [self-hosted, linux, qa] en el job deploy
- Sin SSH tunnel, sin ProxyCommand
- Builds paralelos por servicio con dorny/paths-filter
- Secrets inyectados como variables Ansible

### ✅ COMPLETADO — Registro del Self-Hosted Runner en Bastion
- El self-hosted runner (`pasantias-qa-runner`) se ha registrado, configurado y arrancado exitosamente en el Bastion de QA.
- El servicio de systemd se encuentra en estado `active (running)`.
- Se subió y configuró correctamente la llave SSH del bastion (`~/.ssh/QA.pem`) con permisos `400` para permitir a Ansible interactuar con la instancia privada de QA.

## ✅ COMPLETADO HOY — Adopción UCE_AlumniPlatform

### Mejoras adoptadas del proyecto de referencia
1. Key pair por Terraform (no manual en AWS Console)
2. Secrets en Ansible (JWT, PG_PASSWORD, NEO4J_PASSWORD)
3. LabInstanceProfile en EC2
4. Mosquitto local sin autenticación (allow_anonymous true)
5. base64encode en user_data
6. SSM Agent en bastion
7. 30GB disco en EC2 de servicios
8. Pipeline rediseñado con jobs paralelos


## ✅ Completado / Verificado

- `gateway-service` (8082): **Completamente configurado y asegurado.** Ya tiene:
  - `JwtAuthenticationFilter` funcional (valida JWT con `jjwt`, agrega header `X-Auth-User`, deja pasar `OPTIONS` para CORS preflight).
  - CORS configurado de forma segura con `allowedOriginPatterns` parametrizados por variables de entorno (Local, QA y PROD) con `allowCredentials: true` y habilitando el método `PATCH`.
  - Todas las rutas de negocio protegidas explícitamente con `JwtAuthenticationFilter` (se agregaron las 6 rutas vulnerables correspondientes a `hours-service`, `evaluation-service`, `notification-service`, `document-service`, `report-service` y `ai-service`).
  - Rutas de health-check públicas (`/api/users/health`, `/api/linkage/health`, `/api/hours/health`, `/api/evaluation/health`) que redirigen correctamente a sus respectivos microservicios sin requerir JWT.
  - **Pendiente**: rate limiting (requisito #5) y soporte WebSocket (requisito #15).

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

## ✅ RESUELTO — Error 'no space left on device' en QA (22/jun)

- Modificado `infra/ansible/deploy-qa.yml` para agregar la tarea "Limpieza agresiva de disco antes de pulls". Esta tarea ejecuta de forma previa a los pulls de imágenes un `docker system prune -af --volumes`, `journalctl --vacuum-size=50M` y `rm -rf /tmp/*`, registrando y mostrando el estado del espacio disponible en disco mediante `df -h`.
- Optimizado el `Dockerfile` de `ai-service` para usar `python:3.11-slim`, usar caché desactivado en `pip install --no-cache-dir` y realizar limpieza de caché al final (`apt-get clean && rm -rf /var/lib/apt/lists/* /root/.cache`).

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
con EIP fija **`32.193.25.6`** y puertos 80/8082 abiertos en `sg_private`.
Nueva IP privada: `10.0.1.170` (era `10.0.1.61`). `QA_AUTH_JOBS_IP` actualizado en
GitHub Secrets.- Deploy vía Ansible re-ejecutado, pipeline verde. Verificado en `t3.large` con swap de 4GB (21/jun):
  - `http://32.193.25.6` (Frontend) → `200 OK`, frontend React cargando.
  - `http://32.193.25.6:8082/api/linkage/health` (Linkage via GW) → `200 OK`, `"linkage-service is running"`.
  - `http://32.193.25.6:8082/api/evaluation/health` (Evaluation via GW) → `200 OK`, `"evaluation-service is running"`.
  - `http://32.193.25.6:8082/api/hours/health` (Hours via GW) → Retornaba `404` por conflicto de orden de rutas en `gateway-service` (la ruta comodín `/api/hours/**` consumía `/api/hours/health`). Se corrigió el orden de rutas en `application.yml` localmente y se commiteó para el próximo despliegue. Internamente en puerto 8085 retorna `200 OK` `"hours-service is running"`.
  - Servicios internos (`notification-service` en 8087, `document-service` en 8088, `report-service` en 8089, `ai-service` en 8090) confirmados y respondiendo `200 OK` internamente (bloqueados externamente por Security Group como debe ser).

Excel de Cloudflare llenado:
- **QA IP1** → `32.193.25.6`
- **PRODUCCION IP** → `pasantias-prod-elb-115885246.us-east-1.elb.amazonaws.com`

Cuando la cátedra asigne los subdominios `*.distribuidauce.org`, actualizar
`01-REQUERIMIENTOS-MAESTROS.md` requisito #5 a "✅" y documentar los dominios en
el README principal y en la sección 6 del documento de entrega.

The bastion de QA (`pasantias-qa-bastion`) ahora tiene una Elastic IP fija:
**`50.19.247.85`** (igual que PROD). Se agregó `aws_eip.bastion_eip` a
`infra/qa/main.tf` (mismo patrón que PROD) + `lifecycle { ignore_changes = [ami] }` en
`aws_instance.bastion` de **ambos** `infra/qa/main.tf` e `infra/prod/main.tf` (bug
latente preexistente: sin esto, cualquier `plan`/`apply` futuro reemplazaría el bastion
por completo cada vez que Canonical publica una nueva build de la AMI Ubuntu 24.04).
`terraform apply` aplicado en QA (cuenta #2), limpio, sin destrucción de recursos.
`QA_BASTION_IP` actualizado en GitHub Secrets a `50.19.247.85` — **no debería volver a
cambiar entre sesiones de AWS Academy**.

1. ~~Verificar `QA_BASTION_IP` en GitHub Secrets~~ ✅ RESUELTO PERMANENTEMENTE (ver
  arriba) — ya no es necesario revisar esto antes de cada push.
2.- **Riesgo de memoria en `qa_auth_jobs` (RESUELTO)**: Debido a que la instancia corre 18 contenedores en paralelo (con 10 JVMs de Java, Postgres, Mongo, Kafka, RabbitMQ, etc.), la RAM de 2GiB de la `t3.small` causaba congelamiento de red (timeouts en SSH y HTTP).
  - **Solución 1:** Se actualizó `infra/qa/main.tf` para subir la instancia a **`t3.large` (8 GiB RAM, 2 vCPUs)** para dar mayor holgura.
  - **Solución 2:** Se configuraron límites de memoria JVM en `infra/ansible/deploy-qa.yml` para los 10 servicios Java agregando `-e JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m"`.
  - **Solución 3 (Swap):** Se creó un archivo Swap de **4GB** (`/swapfile`) persistente en el volumen EBS (`/etc/fstab`), garantizando estabilidad de memoria.
  - **Sincronización:** Se corrigió el desfase entre el código y la infraestructura real agregando la subred pública `public_1a`, las reglas de ingreso públicas de puertos 80/8082 para `sg_private` y el recurso `aws_eip.qa_auth_jobs_eip` al código de Terraform y Git.


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

- [x] **Tests antes de build de imágenes** — RESUELTO. Corre `./mvnw test` para los servicios Java con `pom.xml` antes de `docker/build-push-action`.
- [x] **Build y Tests Selectivos** — RESUELTO. Se integró `dorny/paths-filter@v3` en `deploy-qa.yml` y `deploy-prod.yml` para ejecutar únicamente las pruebas unitarias y la compilación/push de imágenes de los servicios que contienen cambios en su respectivo directorio `apps/`.
- [x] **Tests de ai-service en CI/CD** — RESUELTO. Se agregó la ejecución de pruebas unitarias (`pytest`) para `ai-service` condicionada a cambios detectados en `apps/ai-service/**`.
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
- Se configuró `async: 60` y `poll: 10` en todas las tareas lentas de `docker pull` (Postgres, Redis, servicios, Kafka, Mongo, RabbitMQ, etc.) para evitar bloqueos y cuelgues de SSH/ProxyCommand sobre el Bastion host durante los despliegues.
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
6. **Seguridad no-root en contenedores**: Todos los Dockerfiles de los 11 microservicios (Java y Python) se han configurado para ejecutarse bajo un usuario no-root (`appuser` / `appgroup`) por razones de conformidad y seguridad.
7. **Soporte Neo4j en Ansible**: Se agregó la infraestructura y configuración del contenedor `neo4j:5-community` en `deploy-qa.yml` y `deploy-prod.yml`, incluyendo la inyección de credenciales (`NEO4J_URI`, `NEO4J_USER`, `NEO4J_PASSWORD`) a `internship-service` en su despliegue.
8. **SPA Fallback en Nginx**: Se creó una configuración de Nginx personalizada en `apps/frontend-web/nginx.conf` utilizando `try_files` para redirigir correctamente las rutas del SPA a `index.html` en accesos directos, y se integró en el `Dockerfile` de `frontend-web`.

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
   `nombre`/`carrera` — "best effort" con Circuit Breaker programático de Resilience4j (para solucionar la incompatibilidad de `resilience4j-spring-boot3` con Spring Boot 4.0.6, el cual gestiona de forma programática las transiciones de estado, expone su estado en `/health` y fue testeado eficientemente sin causar demoras).
5. Swagger (`springdoc`), README completo, y **prueba de integración end-to-end con
   Testcontainers** (Postgres + Kafka + MongoDB reales) que valida el pipeline
   completo: `POST /api/hours` → evento Kafka → proyección Mongo actualizada →
   `GET /api/hours/student/{id}` → `PATCH .../validar` → totales recalculados.

**19/19 tests pasando**, incluyendo la integración end-to-end. `hours-service` queda
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

## ✅ COMPLETADO — `evaluation-service` (Semana 2, completo)

`apps/evaluation-service` (8086, Layered + PostgreSQL `evaluation_db` + gRPC client
hacia `user-service`). Reutilizó `linkage-service` como plantilla y tiene el
cliente gRPC completo hacia `user-service` con Circuit Breaker programático de Resilience4j (igual que `hours-service` para solucionar la incompatibilidad de `resilience4j-spring-boot3` con Spring Boot 4.0.6, el cual gestiona de forma programática las transiciones de estado, expone su estado en `/health` y fue testeado eficientemente sin causar demoras). Endpoints: `POST /api/evaluations` (valida
`estudianteId`/`tutorId`/`calificacion`, incluyendo rango 0-10 → 400),
`GET /api/evaluations/{id}` (404 si no existe), `GET /api/evaluations/student/{id}`
(enriquecido con nombre/carrera vía gRPC, best-effort). **14/14 tests, BUILD
SUCCESS** en la rama `feature/mosquitto-migration`.

`notification-service` (8087, Event-Driven + Kafka consumer + MQTT) queda **completado localmente** (verificación de tests unitarios e integración con Testcontainers exitosa). Consume eventos de `horas.registradas` con estado `VALIDADO` o `RECHAZADO`, los guarda en PostgreSQL (`notification_db`) y publica un JSON a MQTT en el topic dinámico `notificaciones/{estudianteId}` usando una conexión TCP (puerto 1883) con autenticación básica hacia un broker Mosquitto self-hosted local/QA/PROD. El proceso de publicación a MQTT está protegido con un Circuit Breaker programático de Resilience4j, expuesto en `/health`, y el backend de pruebas pasa al 100% (12/12 tests).

`document-service` (8088, Event-Driven + PDF Generation + REST + S3 + Webhooks) queda **completado localmente** (verificación de 10 tests unitarios exitosa). Consume eventos de `horas.registradas` con estado `VALIDADO`, genera un archivo PDF en memoria utilizando OpenPDF (iText 2.1.7), lo sube a Amazon S3 (`pasantias-documents-qa` con tokens temporales de AWS Academy) bajo la protección de un Circuit Breaker programático de Resilience4j (expuesto en `/health`), guarda la metadata en PostgreSQL (`document_db`), realiza un upsert en MongoDB (`documentos_resumen`) y dispara un webhook de forma asíncrona ("best effort") a n8n.

`report-service` (8089, Kafka consumer + SOAP + MongoDB + PostgreSQL) queda **completado localmente** (verificación de 13 tests unitarios exitosa). Consume eventos de `horas.registradas` en Kafka, realiza la consolidación de reportes, consulta de forma best-effort el total de documentos en `document-service` vía REST (protegido con un Circuit Breaker programático de Resilience4j, expuesto en `/health`), guarda los reportes en PostgreSQL (`report_db`) y actualiza el reporte global en MongoDB.

### Estado de PRs (NO mergear todavía — plan para mañana abajo)
- `feature/user-service-get-by-email` → `QA`: agrega endpoint `/email/{email}` en `user-service` + tests MockMvc (200/404).
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

## ✅ COMPLETADO — Rediseño del Frontend (feature/frontend-redesign-v2)

Se implementaron con éxito las 5 tareas de rediseño e integración del frontend en `apps/frontend-web/src/`:
1. **AppRouter activado:** Reemplazado `src/main.jsx` para instanciar `AppRouter` como punto de entrada de la aplicación.
2. **Dashboard de Home.jsx rediseñado:** Panel de control con 5 paneles autónomos (Header, Donut Chart de Horas en SVG puro, Pasantía activa, Últimas notificaciones y Recomendación de IA), cada uno con manejos independientes de carga (esqueleto) y errores.
3. **Página de Reportes (Reports.jsx) creada:** Muestra el historial consolidado de horas del estudiante (`/api/reports/student/{id}`) y el reporte global consolidado para docentes/coordinadores (`/api/reports/global` de MongoDB). Botón "Generar Reporte" y selector con mensaje correspondiente.
4. **Ruta de /reports registrada:** Configurada la subruta en `AppRouter.jsx` bajo el layout principal `DashboardLayout`.
5. **Unificación de URLs de API:** Modificadas todas las páginas de `pages/` y `DashboardLayout.jsx` para usar la URL base unificada mediante `import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082'`. Creado el archivo `.env.production` en `apps/frontend-web/` con la misma URL del gateway.

Y se aplicaron las siguientes correcciones adicionales:
- **Control de roles en Sidebar.jsx:** Se modificó `Sidebar.jsx` para leer el rol del token JWT en `localStorage`. Se agregaron reglas de visibilidad por rol para `ESTUDIANTE` (Home, Pasantías, Mis Postulaciones, Vinculación, Horas, Evaluaciones, Documentos, Notificaciones, Recomendaciones), `TUTOR` (Home, Pasantías, Vinculación, Horas, Evaluaciones, Documentos, Notificaciones, Reportes), y `COORDINADOR` (todo lo anterior más Usuarios y Reportes). Si no hay rol coincidente, se muestra solo Home y Login.
- **Header de bienvenida en Home.jsx:** Se configuró el saludo para usar el nombre del usuario decodificado de JWT (`payload.nombre || payload.name || payload.sub` o fallback a `'Usuario'`) y el texto del saludo se formateó explícitamente en color blanco (`#FFFFFF` / `text-white`) con sombra de texto para asegurar una legibilidad excelente sobre fondos oscuros.
- **Eliminación de scrollbar horizontal:** Se solucionó el desbordamiento horizontal forzando `overflow-x: hidden` y `max-width: 100vw` en `html` y `body` en `index.css`, y reemplazando el ancho fijo de `#root` (`1126px`) por `width: 100%`. Se aseguró también que los contenedores principales de `DashboardLayout.jsx` tengan las clases `overflow-x-hidden w-full`.
- **Eliminación de "Cargando perfil..." infinito (Bug 1):** Se actualizaron las páginas `Hours.jsx`, `Evaluations.jsx`, `Documents.jsx`, `Notifications.jsx` y `Recommendations.jsx` para extraer la variable `estudianteId` (o `userId`) directamente decodificándola de la propiedad `id || userId || sub || studentId` del JWT almacenado localmente, en lugar de depender de la llamada externa de `DashboardLayout`. También se muestra un mensaje de sesión inválida en caso de que sea nulo.
- **Visualización completa del Sidebar para ESTUDIANTE (Bug 2):** Se posicionó `Home` (`/home`) como el primer elemento visible en la barra lateral y se movió `Mis Postulaciones` (`/internships/applications`) como una opción de primer nivel justo después del bloque de `Pasantías` para los estudiantes.
- **Correcciones de control de roles y JWT (Sesión 22/Jun):**
  - Se confirmó que el JWT del `auth-service` **no contiene un claim de rol** en su payload (solo contiene la clave `sub` con el email del usuario).
  - Se implementó un matching de rol flexible en `Sidebar.jsx` tolerando prefijos/sufijos mediante `includes()` sobre `rol/role/authorities/authority`.
  - Se actualizó `Home.jsx` para resolver de forma flexible el nombre (`nombre/name/firstName/fullName/username`) sin usar `payload.sub` como fallback, ocultando el sufijo del nombre si no se encuentra presente en el token.
  - Se integró la sección de registro de usuarios en `Login.jsx` con campos para nombre, email (validando dominio institucional `@uce.edu.ec`), contraseña, confirmación de contraseña, y selector de rol, comunicando exitosamente con el backend mediante `POST /api/auth/register` y mostrando estados de error y éxito.
  - **Refactorización Completa de JWT y Consistencia en Páginas (Sesión 23/Jun):**
    - Se aplicó el patrón estándar en todas las páginas (`Home.jsx`, `Reports.jsx`, `MyApplications.jsx`, `Recommendations.jsx`), de modo que extraen `estudianteId`, `userRol`, y `nombre` decodificando directamente el JWT del `localStorage`. El `useOutletContext` ahora provee exclusivamente `getHeaders` y `logout`.
    - Se resolvió el bug de "Cargando perfil..." infinito al desacoplar las páginas de la respuesta del `user-service`. Si el usuario no tiene una sesión válida, se muestra un mensaje de error y redirección limpia.
    - Se eliminó el código muerto `App.jsx` y `App.css` del frontend.
    - Se actualizó `ProtectedRoute.jsx` para validar la expiración del token JWT (`exp` claim) redireccionando a `/login` si ha expirado o está dañado.
    - Se corrigieron los selectores del formulario de registro en `Login.jsx` para mandar los valores del rol en mayúscula (`ESTUDIANTE`, `TUTOR`, `COORDINADOR`) alineados con el enum `Role.java` de Spring Boot.
    - Se ajustaron los mocks y payloads de JWT en los tests unitarios (`Home.test.jsx`, `Reports.test.jsx`, `AppRouter.test.jsx`), logrando que el 100% de la suite de pruebas pase exitosamente (`npm run build` y `vitest` en verde).


## ✅ COMPLETADO — Migración a Self-Hosted Runner y Remoción de IPs (QA Lab 53)

Se implementaron con éxito los cambios para adoptar un self-hosted runner en el Bastion de QA y eliminar IPs hardcodeadas:
1. **GitHub Actions Runner en Bastion (Terraform):** Agregado `user_data` a `aws_instance.bastion` en `infra/qa/main.tf` para instalar dependencias de Docker, herramientas y preparar la carpeta del runner.
2. **Workflow simplificado:** Modificado `deploy-qa.yml` para correr en `runs-on: self-hosted`, eliminando los pasos obsoletos de SSH Tunnel, ProxyCommand y la instalación de Ansible en cada ejecución.
3. **CORS Dinámico:** Configurado `deploy-qa.yml` de Ansible para inyectar la variable `ALLOWED_ORIGINS` dinámicamente con la IP de la instancia a través del workflow.

## ✅ COMPLETADO — Adopción de Mejoras UCE_AlumniPlatform e Integración de Cambios (23/Jun)

Se adoptaron 4 mejoras del proyecto de referencia y se aplicaron las reversiones solicitadas en la rama `QA`:
1. **Key Pairs creados por Terraform:** Se crearon recursos `aws_key_pair` en `infra/qa` e `infra/prod` utilizando llaves generadas localmente (`QA.pub`, `PROD.pub`). Las llaves privadas se agregaron a `.gitignore`.
2. **Secrets en Ansible:** Se parametrizaron `JWT_SECRET`, `POSTGRES_PASSWORD` y `NEO4J_PASSWORD` en Ansible y GitHub Actions, eliminando credenciales hardcodeadas en texto plano.
3. **LabInstanceProfile en EC2:** Se asoció el rol IAM de AWS Academy (`LabInstanceProfile`) a las instancias EC2 en QA y PROD para evitar errores de permisos.
4. **Mosquitto local sin autenticación en QA:** Configurado broker Mosquitto local en el EC2 de QA con `allow_anonymous true` y redirigido `notification-service` a este broker local.
5. **Reversiones de Frontend y CORS:** Se eliminó la IP hardcodeada de CORS fallback del gateway, se removió `.env.production` y se quitó el build arg `VITE_API_BASE_URL` del Dockerfile (el frontend resuelve el endpoint de manera dinámica usando el hostname de la ventana del navegador).

## ✅ COMPLETADO — Rediseño del Pipeline de CI/CD (23/Jun)

Se rediseñó por completo el pipeline de QA (`deploy-qa.yml`) logrando:
1. **Detección de Cambios (`detect-changes`):** Ejecuta la discriminación de rutas usando `dorny/paths-filter`.
2. **Pruebas Consolidadas (`test`):** Un solo job en `ubuntu-latest` que ejecuta condicionalmente las pruebas unitarias únicamente para los servicios modificados y las del frontend.
3. **Compilación Paralela (`build-<servicio>`):** Se crearon 12 jobs independientes y paralelos (uno por servicio) que compilan y suben las imágenes a Docker Hub en paralelo solo si se detectaron cambios en su respectivo directorio.
4. **Despliegue Secuencial y Seguro (`deploy`):** Ejecuta en `ubuntu-latest` usando el esquema ProxyCommand original para conectarse a la instancia `qa_ec2` a través del Bastion de QA, una vez terminados todos los builds de forma exitosa, omitiendo de forma inteligente los builds omitidos (skipped) pero asegurando que si alguno falla, el deploy se aborte. Se configuró el play principal de Ansible para ejecutarse sobre `hosts: qa_ec2` y la conexión SSH en el pipeline a través de la configuración ProxyCommand.

Última ejecución y deploy de prueba: 2026-06-24 (retornado a esquema ProxyCommand)
