terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "estado-pasantias-gisse-2026" 
    key    = "prod/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = "us-east-1"
}

# --- 1. THE SECURITY GUARD (PRODUCTION PORTS 9080 AND 9081) ---
resource "aws_security_group" "prod_sg" {
  name        = "prod_security_group"
  description = "Allow web traffic and Java microservices in Production"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 9080
    to_port     = 9081 
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

# --- 2. THE PRODUCTION SERVER ---
resource "aws_instance" "backend_prod_server" {
  ami           = "ami-0c7217cdde317cfec"
  instance_type = "t2.micro"

  vpc_security_group_ids = [aws_security_group.prod_sg.id]

  user_data_replace_on_change = true

  user_data = <<-EOF
              #!/bin/bash
              export HOME=/root
              
              # --- SENIOR TRICK: Create 2GB of Virtual Memory (Swap) ---
              fallocate -l 2G /swapfile
              chmod 600 /swapfile
              mkswap /swapfile
              swapon /swapfile

              # 1. Update and install basic dependencies
              apt-get update -y
              apt-get install -y openjdk-17-jdk apache2 git curl

              # 2. Install Node.js
              curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
              apt-get install -y nodejs

              # 3. Encender el servidor web
              systemctl start apache2
              systemctl enable apache2

              # 4. Download your code from the MAIN branch (Production)
              rm -rf /tmp/proyecto
              git clone -b main https://github.com/gisselamuzo49-commits/FINAL-PROJECT-.git /tmp/proyecto

              # 5. Frontend - Inject Production ports before compiling
              cd /tmp/proyecto/apps/frontend-web
              echo "VITE_AUTH_PORT=9080" > .env
              echo "VITE_INTERNSHIP_PORT=9081" >> .env
              npm install > /var/www/html/log_npm.txt 2>&1
              npm run build >> /var/www/html/log_npm.txt 2>&1
              cp -r dist/* /var/www/html/

              # 6. Auth Service (Puerto 9080)
              cd /tmp/proyecto/apps/auth-service
              tr -d '\r' < mvnw > mvnw.lf && mv mvnw.lf mvnw # <-- Convertir CRLF a LF
              chmod +x mvnw
              echo "--- Starting auth-service compilation ---" > /var/www/html/log_auth.txt
              ./mvnw clean install -DskipTests >> /var/www/html/log_auth.txt 2>&1
              if ls target/*.jar >/dev/null 2>&1; then
                  echo "--- Starting auth-service  ---" >> /var/www/html/log_auth.txt
                  nohup java -jar target/*.jar --server.port=9080 >> /var/www/html/log_auth.txt 2>&1 &
              else
                  echo "ERROR: Could not compile auth-service JAR file" >> /var/www/html/log_auth.txt
              fi

              # 7. Internship Service (Puerto 9081)
              cd /tmp/proyecto/apps/internship-service
              tr -d '\r' < mvnw > mvnw.lf && mv mvnw.lf mvnw # <-- Convertir CRLF a LF
              chmod +x mvnw
              echo "--- Starting internship-service compilation ---" > /var/www/html/log_internship.txt
              ./mvnw clean install -DskipTests >> /var/www/html/log_internship.txt 2>&1
              if ls target/*.jar >/dev/null 2>&1; then
                  echo "--- Starting internship-service ---" >> /var/www/html/log_internship.txt
                  nohup java -jar target/*.jar --server.port=9081 >> /var/www/html/log_internship.txt 2>&1 &
              else
                  echo "ERROR: Could not compile internship-service JAR file" >> /var/www/html/log_internship.txt
              fi

              # 8. Restart Apache to serve the latest files
              systemctl restart apache2

              # ----- FORCE REDEPLOY ON TF ----- 
              # redeploy 2026-06-01-v2
              EOF

  tags = {
    Name        = "Servidor-Backend-PROD"
    Environment = "PROD"
    Project     = "Sistema de Pasantias"
  }
}
