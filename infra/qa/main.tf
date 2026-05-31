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

# --- NUEVO: 1. EL GUARDIA DE SEGURIDAD ---
resource "aws_security_group" "qa_sg" {
  name        = "qa_security_group"
  description = "Permitir trafico web y backend"

  # Abrir Puerto 80 para la Web (HTTP)
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Abrir Puerto 8080 para tu futuro backend en Java (Spring Boot)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Permitir que el servidor navegue a internet para descargar cosas
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# --- 2. TU SERVIDOR ACTUALIZADO ---
resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec" 
  instance_type = "t2.micro"              
  
  # Asignarle el guardia de seguridad a este servidor
  vpc_security_group_ids = [aws_security_group.qa_sg.id]

  # NUEVO: Instrucciones de encendido automático
  user_data = <<-EOF
              #!/bin/bash
              # Actualizar el sistema
              yum update -y
              # Instalar Java 17 y un servidor web básico
              yum install -y java-17-amazon-corretto httpd
              systemctl start httpd
              systemctl enable httpd
              # Crear una página de prueba
              echo "<h1>¡Felicidades Gissela! Tu servidor automatizado con GitOps esta vivo.</h1><p>Java ya esta instalado y listo para el backend de tu Sistema de Pasantias.</p>" > /var/www/html/index.html
              EOF

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}