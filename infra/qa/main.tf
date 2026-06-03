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

              # 2. Crear directorios para persistir bases de datos SQLite locales
              mkdir -p /var/lib/pasantias

              # 3. Detener y remover contenedores viejos si existen
              docker stop auth-service internship-service frontend-web || true
              docker rm auth-service internship-service frontend-web || true

              # 4. Correr contenedores con límites de memoria
              docker run -d \
                --name auth-service \
                -p 8080:8080 \
                -m 300m \
                -v /var/lib/pasantias/auth.db:/app/auth.db \
                --restart always \
                gisselamuzo49/auth-service:latest

              docker run -d \
                --name internship-service \
                -p 8081:8081 \
                -m 300m \
                -v /var/lib/pasantias/internship.db:/app/internship.db \
                --restart always \
                gisselamuzo49/internship-service:latest

              docker run -d \
                --name frontend-web \
                -p 80:80 \
                -m 150m \
                --restart always \
                gisselamuzo49/frontend-web:latest

              # ----- FORCE REDEPLOY ON TF ----- 
              # redeploy docker-v1
              EOF

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}
