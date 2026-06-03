terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "estado-pasantias-gisse-2026" 
    key    = "qa/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = "us-east-1"
}

# --- 1. THE SECURITY GUARD (WE ADD 8081) ---
resource "aws_security_group" "qa_sg" {
  name        = "qa_security_group"
  description = "Allow web traffic and Java microservices"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8081 
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# --- 2. THE SERVER ---
resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec"
  instance_type = "t2.micro"

  vpc_security_group_ids = [aws_security_group.qa_sg.id]

  user_data_replace_on_change = true

  user_data = <<-EOF
              #!/bin/bash
              export HOME=/root
              
              # 1. Instalar Docker
              apt-get update -y
              apt-get install -y docker.io curl
              systemctl start docker
              systemctl enable docker

              # 2. Corregir y asegurar archivos SQLite válidos (evitar que Docker monte directorios)
              mkdir -p /var/lib/pasantias
              for db_file in auth.db internship.db; do
                if [ -d "/var/lib/pasantias/$db_file" ]; then
                  rm -rf "/var/lib/pasantias/$db_file"
                fi
                touch "/var/lib/pasantias/$db_file"
              done

              # 3. Configurar Directorio de Diagnóstico
              mkdir -p /var/log/pasantias-diag
              cat << 'ERR' > /var/log/pasantias-diag/index.html
              <!DOCTYPE html>
              <html>
              <head>
                <meta http-equiv="refresh" content="5">
                <title>Diagnóstico Pasantías (Inicializando...)</title>
                <style>
                  body { font-family: monospace; background: #121212; color: #ffeb3b; padding: 20px; }
                </style>
              </head>
              <body>
                <h1>📋 Iniciando sistema de diagnóstico y descargando contenedores...</h1>
                <p>Por favor espera, la página se recargará automáticamente.</p>
              </body>
              </html>
              ERR

              # 4. Detener y remover contenedores viejos si existen
              docker stop auth-service internship-service frontend-web || true
              docker rm auth-service internship-service frontend-web || true

              # 5. Descargar nuevas imágenes y arrancar contenedores
              docker pull gdmuzo/auth-service:qa
              docker pull gdmuzo/internship-service:qa
              docker pull gdmuzo/frontend-web:qa

              docker run -d \
                --name auth-service \
                -p 8080:8080 \
                -m 300m \
                -v /var/lib/pasantias/auth.db:/app/auth.db \
                --restart always \
                gdmuzo/auth-service:qa

              docker run -d \
                --name internship-service \
                -p 8081:8081 \
                -m 300m \
                -v /var/lib/pasantias/internship.db:/app/internship.db \
                --restart always \
                gdmuzo/internship-service:qa

              docker run -d \
                --name frontend-web \
                -p 80:80 \
                -m 150m \
                -v /var/log/pasantias-diag:/usr/share/nginx/html/status \
                --restart always \
                gdmuzo/frontend-web:qa

              # 6. Bucle de actualización para el Dashboard de Diagnóstico
              (
              while true; do
                cat << 'DASHBOARD' > /var/log/pasantias-diag/index.html
              <!DOCTYPE html>
              <html>
              <head>
                <meta http-equiv="refresh" content="10">
                <title>Estado de Contenedores QA</title>
                <style>
                  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace; background: #0f172a; color: #e2e8f0; padding: 20px; margin: 0; }
                  .container { max-width: 1200px; margin: 0 auto; }
                  h1 { color: #38bdf8; border-bottom: 2px solid #334155; padding-bottom: 10px; }
                  h2 { color: #f59e0b; margin-top: 30px; font-size: 1.2rem; }
                  pre { background: #1e293b; padding: 15px; border-radius: 8px; overflow-x: auto; border: 1px solid #334155; color: #34d399; font-size: 0.9rem; }
                  .footer { margin-top: 40px; text-align: center; color: #64748b; font-size: 0.8rem; }
                </style>
              </head>
              <body>
                <div class="container">
                  <h1>📋 Dashboard de Diagnóstico - QA (Actualizado: $(date -u) UTC)</h1>
                  
                  <h2>🐳 Estado de los Contenedores Docker</h2>
                  <pre>$(docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}")</pre>

                  <h2>🔑 Logs del Servicio de Autenticación (auth-service)</h2>
                  <pre>$(docker logs --tail 30 auth-service 2>&1)</pre>

                  <h2>💼 Logs del Servicio de Pasantías (internship-service)</h2>
                  <pre>$(docker logs --tail 30 internship-service 2>&1)</pre>

                  <h2>🌐 Logs del Servidor Frontend Nginx</h2>
                  <pre>$(docker logs --tail 30 frontend-web 2>&1)</pre>
                  
                  <div class="footer">Recarga automática cada 10 segundos</div>
                </div>
              </body>
              </html>
              DASHBOARD
                sleep 10
              done
              ) &
              
              # ----- FORCE REDEPLOY ON TF ----- 
              # redeploy docker-v3
              EOF

  tags = {
    Name        = "Backend-QA-Server"
    Environment = "QA"
    Project     = "System for Managing Internships and University Outreach Projects"
  }
}
