# ─────────────────────────────────────────
# TERRAFORM BACKEND — S3 remote state QA
# ─────────────────────────────────────────
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket       = "estado-pasantias-gisse-2026"
    key          = "qa/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
    encrypt      = true
  }
}

provider "aws" {
  region = "us-east-1"
}

# ─────────────────────────────────────────
# VPC
# ─────────────────────────────────────────
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = { Name = "pasantias-qa-vpc" }
}

# ─────────────────────────────────────────
# SUBNETS
# ─────────────────────────────────────────
resource "aws_subnet" "public_1a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true
  tags                    = { Name = "pasantias-qa-public-1a" }
}

resource "aws_subnet" "private_1a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "us-east-1a"
  tags              = { Name = "pasantias-qa-private-1a" }
}

# ─────────────────────────────────────────
# INTERNET GATEWAY
# ─────────────────────────────────────────
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "pasantias-qa-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = { Name = "pasantias-qa-public-rt" }
}

resource "aws_route_table_association" "public_1a" {
  subnet_id      = aws_subnet.public_1a.id
  route_table_id = aws_route_table.public.id
}

# ─────────────────────────────────────────
# NAT GATEWAY
# ─────────────────────────────────────────
resource "aws_eip" "nat" {
  domain = "vpc"
  tags   = { Name = "pasantias-qa-nat-eip" }
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_1a.id
  tags          = { Name = "pasantias-qa-nat" }
  depends_on    = [aws_internet_gateway.igw]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }
  tags = { Name = "pasantias-qa-private-rt" }
}

resource "aws_route_table_association" "private_1a" {
  subnet_id      = aws_subnet.private_1a.id
  route_table_id = aws_route_table.private.id
}

# ─────────────────────────────────────────
# SECURITY GROUPS
# ─────────────────────────────────────────

# Bastion: solo SSH desde internet
resource "aws_security_group" "sg_bastion" {
  name        = "pasantias-qa-bastion"
  description = "SSH access to bastion host"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "pasantias-qa-bastion" }
}

# Private: servicios accesibles dentro de la VPC + SSH desde bastion
resource "aws_security_group" "sg_private" {
  name        = "pasantias-qa-private"
  description = "Private EC2 - todos los servicios"
  vpc_id      = aws_vpc.main.id

  # Todo el tráfico interno de la VPC
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["10.0.0.0/16"]
  }
  # SSH solo desde el bastion
  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_bastion.id]
  }
  # Acceso HTTP público al frontend
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Frontend web"
  }
  # Acceso HTTP público al gateway (único punto de entrada a los microservicios)
  ingress {
    from_port   = 8082
    to_port     = 8082
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Gateway API"
  }
  # Acceso público a la UI y webhooks de n8n
  ingress {
    from_port   = 5678
    to_port     = 5678
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "n8n UI and Webhooks"
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "pasantias-qa-private" }
}

# ─────────────────────────────────────────
# KEY PAIR — QA key ya creada en AWS
# ─────────────────────────────────────────
data "aws_key_pair" "qa_key" {
  key_name = "QA"
}

# ─────────────────────────────────────────
# AMI — Ubuntu 24.04 LTS (última versión)
# ─────────────────────────────────────────
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd*ubuntu*24.04*amd64*"]
  }
}

# ─────────────────────────────────────────
# BASTION HOST — jump host público
# NOTA: la EIP permanece fija entre sesiones de AWS Academy.
# ─────────────────────────────────────────
resource "aws_instance" "bastion" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.micro"
  subnet_id              = aws_subnet.public_1a.id
  key_name               = data.aws_key_pair.qa_key.key_name
  vpc_security_group_ids = [aws_security_group.sg_bastion.id]
  tags                   = { Name = "pasantias-qa-bastion" }

  lifecycle {
    ignore_changes = [ami]
  }
}

# Elastic IP fija — permanece aunque la instancia se reinicie
resource "aws_eip" "bastion_eip" {
  instance   = aws_instance.bastion.id
  domain     = "vpc"
  depends_on = [aws_internet_gateway.igw]
  tags       = { Name = "pasantias-qa-bastion-eip" }
}

# ─────────────────────────────────────────
# EC2 — QA Services (todos los microservicios)
# ─────────────────────────────────────────
resource "aws_instance" "qa_auth_jobs" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.large"
  subnet_id              = aws_subnet.public_1a.id
  key_name               = data.aws_key_pair.qa_key.key_name
  vpc_security_group_ids = [aws_security_group.sg_private.id]

  user_data = <<-EOF
    #!/bin/bash
    apt-get update -y
    apt-get install -y ca-certificates curl gnupg apt-transport-https
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu noble stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ubuntu
  EOF

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = { Name = "pasantias-qa-ec2-services" }

  lifecycle {
    ignore_changes = [user_data, ami]
  }
}

# Elastic IP fija para la instancia de servicios
resource "aws_eip" "qa_auth_jobs_eip" {
  instance   = aws_instance.qa_auth_jobs.id
  domain     = "vpc"
  depends_on = [aws_internet_gateway.igw]
  tags       = { Name = "pasantias-qa-auth-jobs-eip" }
}

# ─────────────────────────────────────────
# S3 BUCKET — para document-service
# Se crea aquí para que exista antes del primer deploy.
# AWS Academy: este bucket persiste entre sesiones.
# ─────────────────────────────────────────
resource "aws_s3_bucket" "documents_qa" {
  bucket        = "pasantias-documents-qa"
  force_destroy = true
  tags          = { Name = "pasantias-documents-qa" }
}

resource "aws_s3_bucket_ownership_controls" "documents_qa" {
  bucket = aws_s3_bucket.documents_qa.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "documents_qa" {
  depends_on = [aws_s3_bucket_ownership_controls.documents_qa]
  bucket     = aws_s3_bucket.documents_qa.id
  acl        = "private"
}

resource "aws_vpc_endpoint" "s3_qa" {
  vpc_id       = aws_vpc.main.id
  service_name = "com.amazonaws.us-east-1.s3"

  route_table_ids = [aws_route_table.private.id]

  tags = {
    Name = "pasantias-qa-s3-endpoint"
  }
}

# ─────────────────────────────────────────
# OUTPUTS
# Comandos para obtener los valores:
#   terraform apply -refresh-only
#   terraform output
# ─────────────────────────────────────────
output "bastion_public_ip" {
  description = "QA Bastion EIP — fija entre sesiones de AWS Academy, usar como QA_BASTION_IP en GitHub Secrets"
  value       = aws_eip.bastion_eip.public_ip
}

output "qa_auth_jobs_private_ip" {
  description = "IP privada del EC2 de servicios — usar como QA_AUTH_JOBS_IP en GitHub Secrets"
  value       = aws_instance.qa_auth_jobs.private_ip
}

output "qa_auth_jobs_public_ip" {
  description = "IP pública del EC2 de servicios (asociada a la EIP)"
  value       = aws_eip.qa_auth_jobs_eip.public_ip
}

output "documents_bucket_qa" {
  description = "Nombre del bucket S3 para document-service QA"
  value       = aws_s3_bucket.documents_qa.bucket
}
