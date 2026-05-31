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

# =========================================================
# --- 3. EL ALOJAMIENTO PARA TU FRONTEND (S3 WEB) ---
# =========================================================

# 3.1 Crear el bucket (el nombre debe ser único en el mundo)
resource "aws_s3_bucket" "frontend_bucket" {
  bucket = "frontend-pasantias-gisse-2026" # <-- PERSONALIZA ESTE NOMBRE
}

# 3.2 Configurar el bucket para que funcione como un sitio web
resource "aws_s3_bucket_website_configuration" "frontend_website" {
  bucket = aws_s3_bucket.frontend_bucket.id

  index_document {
    suffix = "index.html"
  }
  error_document {
    key = "index.html" # Truco vital para que React funcione sin errores 404
  }
}

# 3.3 Apagar el candado de seguridad para que internet pueda ver la página
resource "aws_s3_bucket_public_access_block" "frontend_public_access" {
  bucket = aws_s3_bucket.frontend_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# 3.4 Crear la regla que permite a cualquier persona leer tu web
resource "aws_s3_bucket_policy" "frontend_policy" {
  bucket = aws_s3_bucket.frontend_bucket.id
  depends_on = [aws_s3_bucket_public_access_block.frontend_public_access]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.frontend_bucket.arn}/*"
      },
    ]
  })
}

# 3.5 ¡Imprimir el link público al final!
output "enlace_frontend" {
  description = "El link público para ver tu frontend en internet"
  value       = "http://${aws_s3_bucket.frontend_bucket.bucket}.s3-website-us-east-1.amazonaws.com"
}