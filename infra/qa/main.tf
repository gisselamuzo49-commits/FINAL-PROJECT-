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

# --- 1. EL GUARDIA DE SEGURIDAD (AÑADIMOS EL 8081) ---
resource "aws_security_group" "qa_sg" {
  name        = "qa_security_group"
  description = "Permitir trafico web y microservicios Java"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8081 # <--- Abrimos ambos puertos para los microservicios
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

# --- 2. EL SERVIDOR ---
resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec"
  instance_type = "t2.micro"

  vpc_security_group_ids = [aws_security_group.qa_sg.id]

  user_data_replace_on_change = true

  user_data = <<-EOF
              #!/bin/bash
              # --- TRUCO SENIOR: Crear 2GB de Memoria Virtual (Swap) ---
              fallocate -l 2G /swapfile
              chmod 600 /swapfile
              mkswap /swapfile
              swapon /swapfile

              # 1. Actualizar e instalar dependencias básicas
              apt-get update -y
              apt-get install -y openjdk-17-jdk apache2 git curl

              # 2. Instalar Node.js
              curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
              apt-get install -y nodejs

              # 3. Encender el servidor web
              systemctl start apache2
              systemctl enable apache2

              # 4. Descargar tu código
              rm -rf /tmp/proyecto
              git clone -b QA https://github.com/gisselamuzo49-commits/FINAL-PROJECT-.git /tmp/proyecto

              # 5. Frontend
              cd /tmp/proyecto/apps/frontend-web
              npm install > /var/www/html/log_npm.txt 2>&1
              npm run build >> /var/www/html/log_npm.txt 2>&1
              cp -r dist/* /var/www/html/

              # 6. Auth Service
              cd /tmp/proyecto/apps/auth-service
              tr -d '\r' < mvnw > mvnw.lf && mv mvnw.lf mvnw # <-- Convertir CRLF a LF
              chmod +x mvnw
              echo "--- Iniciando compilación de auth-service ---" > /var/www/html/log_auth.txt
              ./mvnw clean install -DskipTests >> /var/www/html/log_auth.txt 2>&1
              if ls target/*.jar >/dev/null 2>&1; then
                  echo "--- Iniciando auth-service ---" >> /var/www/html/log_auth.txt
                  nohup java -jar target/*.jar >> /var/www/html/log_auth.txt 2>&1 &
              else
                  echo "ERROR: No se pudo compilar el archivo JAR de auth-service" >> /var/www/html/log_auth.txt
              fi

              # 7. Internship Service
              cd /tmp/proyecto/apps/internship-service
              tr -d '\r' < mvnw > mvnw.lf && mv mvnw.lf mvnw # <-- Convertir CRLF a LF
              chmod +x mvnw
              echo "--- Iniciando compilación de internship-service ---" > /var/www/html/log_internship.txt
              ./mvnw clean install -DskipTests >> /var/www/html/log_internship.txt 2>&1
              if ls target/*.jar >/dev/null 2>&1; then
                  echo "--- Iniciando internship-service ---" >> /var/www/html/log_internship.txt
                  nohup java -jar target/*.jar >> /var/www/html/log_internship.txt 2>&1 &
              else
                  echo "ERROR: No se pudo compilar el archivo JAR de internship-service" >> /var/www/html/log_internship.txt
              fi

              # 8. Reiniciar Apache para que sirva los últimos archivos
              systemctl restart apache2

              # ----- FORZAR REDEPLOY EN TF ----- 
              # redeploy 2026‑05‑31‑v3
              EOF

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}
