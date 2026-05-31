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

# --- 1. EL GUARDIA DE SEGURIDAD ---
resource "aws_security_group" "qa_sg" {
  name        = "qa_security_group"
  description = "Permitir trafico web y backend"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
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

# --- 2. TU SERVIDOR EC2 (CON FRONTEND Y BACKEND) ---
resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec" 
  instance_type = "t2.micro"              
  
  vpc_security_group_ids = [aws_security_group.qa_sg.id]

  user_data_replace_on_change = true

  user_data = <<-EOF
              #!/bin/bash
              # 1. Actualizar e instalar dependencias basicas
              apt-get update -y
              apt-get install -y openjdk-17-jdk apache2 git curl

              # 2. Instalar Node.js (el motor para React/Vite)
              curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
              apt-get install -y nodejs

              # 3. Encender el servidor web
              systemctl start apache2
              systemctl enable apache2

              # 4. Descargar tu codigo desde GitHub
              git clone https://github.com/gisselamuzo49-commits/FINAL-PROJECT-.git /tmp/proyecto

              # 5. Entrar a la carpeta del Frontend, instalar y empaquetar
              cd /tmp/proyecto/apps/frontend-web
              npm install
              npm run build

              # 6. Mover el proyecto final a la carpeta publica de Apache
              rm -rf /var/www/html/*
              cp -r dist/* /var/www/html/
              EOF

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}