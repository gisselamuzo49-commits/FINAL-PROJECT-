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

# --- 1. EL GUARDIA DE SEGURIDAD (¡Este bloque faltaba!) ---
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

# --- 2. TU SERVIDOR ACTUALIZADO ---
resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec" 
  instance_type = "t2.micro"              
  
  vpc_security_group_ids = [aws_security_group.qa_sg.id]

  user_data_replace_on_change = true

  user_data = <<-EOF
              #!/bin/bash
              apt-get update -y
              apt-get install -y openjdk-17-jdk apache2
              systemctl start apache2
              systemctl enable apache2
              echo "<h1>¡Felicidades Gissela! Tu servidor automatizado con GitOps esta vivo.</h1><p>Java ya esta instalado y listo para el backend de tu Sistema de Pasantias.</p>" > /var/www/html/index.html
              EOF

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}