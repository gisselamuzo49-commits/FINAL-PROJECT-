# Guía de Recuperación y Despliegue de AWS 🚀

Esta guía contiene los pasos exactos y simplificados para volver a levantar los ambientes de **QA** y **Producción (PROD)** desde cero cuando inicies una nueva sesión en tu laboratorio de AWS Academy.

---

## 📋 Pasos para Levantar Todo el Sistema

### Paso 1: Iniciar el Laboratorio en AWS Academy
1. Ingresa a tu plataforma de AWS Academy.
2. Haz clic en **Start Lab** y espera a que el indicador se ponga en **verde**.
3. Haz clic en el botón **AWS Details** para abrir la información del laboratorio.

---

### Paso 2: Actualizar las Credenciales en GitHub
Como las credenciales temporales de AWS expiran y cambian en cada inicio de laboratorio, debes actualizarlas en GitHub para que los pipelines de GitOps funcionen:
1. En la ventana de **AWS Details** del laboratorio, copia los valores de la sección **AWS CLI**:
   * `AWS_ACCESS_KEY_ID`
   * `AWS_SECRET_ACCESS_KEY`
   * `AWS_SESSION_TOKEN`
2. Ve a tu repositorio en GitHub.
3. Entra a **Settings** -> **Secrets and variables** -> **Actions**.
4. Edita y actualiza los tres secretos correspondientes con los nuevos valores.

---

### Paso 3: Crear el Bucket de S3 para el Estado de Terraform
Si el laboratorio se destruyó o se reinició por completo, el bucket de S3 donde se almacena el estado de Terraform habrá desaparecido y debes recrearlo:
1. Haz clic en el botón **AWS** en los detalles del laboratorio para abrir la Consola Web de AWS.
2. Busca el servicio **S3**.
3. Haz clic en **Create bucket** y crea un bucket con el siguiente nombre exacto:
   * **Name:** `estado-pasantias-gisse-2026`
   * **Region:** `us-east-1` (N. Virginia)
4. Deja el resto de opciones por defecto y haz clic en **Create**.

---

### Paso 4: Desplegar el Ambiente de QA (Rama `QA`)
Para levantar de forma automática la infraestructura de QA en puertos `8080/8081`:
1. Abre tu código local en VS Code en la rama `QA`.
2. Ve al archivo `infra/qa/main.tf` y cambia el número de versión al final del archivo para forzar a Terraform a realizar el despliegue (ej. cambia `v5` por `v6`):
   ```hcl
   # ----- FORZAR REDEPLOY EN TF ----- 
   # redeploy 2026-05-31-v6
   ```
3. Guarda el archivo y ejecuta en tu terminal:
   ```bash
   git add infra/qa/main.tf
   git commit -m "chore: force QA redeploy after lab restart"
   git push origin QA
   ```
4. El pipeline `GitOps - Infraestructura QA` comenzará a ejecutarse en GitHub. Espera unos **3 a 5 minutos**.

---

### Paso 5: Desplegar el Ambiente de Producción (Rama `main`)
Para levantar de forma automática la infraestructura de Producción en puertos `9080/9081`:
1. Cambia a la rama `main` en tu entorno local (o haz el push de los cambios de QA a main):
   ```bash
   git push origin QA:main
   ```
2. Si deseas forzar un cambio de versión directamente en producción, ve a `infra/prod/main.tf`, incrementa el número de versión en el comentario al final (ej. `v1` por `v2`), haz commit y push a `main`:
   ```bash
   git add infra/prod/main.tf
   git commit -m "chore: force PROD redeploy after lab restart"
   git push origin main
   ```
3. El pipeline `GitOps - Infraestructura PROD` se ejecutará en GitHub. Espera unos **3 a 5 minutos**.

---

## 🔍 ¿Cómo ingresar y verificar tus aplicaciones?

Debido al diseño dinámico que implementamos, **no necesitas cambiar ninguna IP pública en el código**. El sistema detectará todo automáticamente. Solo sigue estos pasos para entrar:

1. Ve a la consola web de AWS, entra al servicio **EC2** y luego a **Instances**.
2. Verás dos instancias encendidas:
   * 💻 **Servidor-Backend-QA**
   * 💻 **Servidor-Backend-PROD**
3. Copia las direcciones IPs públicas de cada una:
   * **QA:** Abre `http://<IP_PUBLICA_QA>` en tu navegador. Tus microservicios se conectarán automáticamente en los puertos **8080** y **8081**.
   * **PROD:** Abre `http://<IP_PUBLICA_PROD>` en tu navegador. Tus microservicios se conectarán automáticamente en los puertos **9080** y **9081**.

---

## 📝 Verificación de Logs
Si deseas verificar que los servicios y sentencias SQL estén corriendo correctamente, una vez que ingreses a la IP pública de cualquiera de los dos servidores, puedes ver sus consolas en tiempo real abriendo las siguientes rutas en el navegador:
* **Logs de Auth:** `http://<IP_PUBLICA>/log_auth.txt`
* **Logs de Pasantías:** `http://<IP_PUBLICA>/log_internship.txt`
