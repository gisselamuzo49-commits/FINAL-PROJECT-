terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # ¡NUEVO! Conexión al cofre de S3
  backend "s3" {
    bucket = "estado-pasantias-gisse-2026" # <-- PON EL NOMBRE DE TU BUCKET AQUÍ
    key    = "qa/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "backend_qa_server" {
  ami           = "ami-0c7217cdde317cfec" 
  instance_type = "t2.micro"              

  tags = {
    Name        = "Servidor-Backend-QA"
    Environment = "QA"
    Project     = "Sistema de Pasantias"
  }
}