# ─────────────────────────────────────────
# TERRAFORM BACKEND — S3 remote state PROD
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
    bucket       = "estado-pasantias-gisse-lab53"
    key          = "prod/terraform.tfstate"
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
  tags                 = { Name = "pasantias-prod-vpc" }
}

# ─────────────────────────────────────────
# SUBNETS — 2 AZs requeridas para el ELB
# ─────────────────────────────────────────
resource "aws_subnet" "public_1a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true
  tags                    = { Name = "pasantias-prod-public-1a" }
}

resource "aws_subnet" "public_1b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "us-east-1b"
  map_public_ip_on_launch = true
  tags                    = { Name = "pasantias-prod-public-1b" }
}

resource "aws_subnet" "private_1a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "us-east-1a"
  tags              = { Name = "pasantias-prod-private-1a" }
}

# ─────────────────────────────────────────
# INTERNET GATEWAY
# ─────────────────────────────────────────
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "pasantias-prod-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = { Name = "pasantias-prod-public-rt" }
}

resource "aws_route_table_association" "public_1a" {
  subnet_id      = aws_subnet.public_1a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_1b" {
  subnet_id      = aws_subnet.public_1b.id
  route_table_id = aws_route_table.public.id
}

# ─────────────────────────────────────────
# NAT GATEWAY — salida a internet para la
#               subnet privada (docker pull)
# ─────────────────────────────────────────
resource "aws_eip" "nat_eip" {
  domain     = "vpc"
  tags       = { Name = "pasantias-prod-nat-eip" }
  depends_on = [aws_internet_gateway.igw]
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat_eip.id
  subnet_id     = aws_subnet.public_1a.id
  tags          = { Name = "pasantias-prod-nat" }
  depends_on    = [aws_internet_gateway.igw]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }
  tags = { Name = "pasantias-prod-private-rt" }
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
  name        = "pasantias-prod-bastion"
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
  tags = { Name = "pasantias-prod-bastion" }
}

# ELB: puertos públicos 80 (frontend), 8080 (auth), 8081 (internship)
resource "aws_security_group" "sg_elb" {
  name        = "pasantias-prod-elb"
  description = "Application Load Balancer - frontend, auth, internship"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Frontend web"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "Auth service"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "Internship service"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "User service"
    from_port   = 8083
    to_port     = 8083
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "Gateway service"
    from_port   = 8082
    to_port     = 8082
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "Linkage service"
    from_port   = 8084
    to_port     = 8084
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "pasantias-prod-elb" }
}

# Private: servicios accesibles solo desde ELB y bastion
resource "aws_security_group" "sg_private" {
  name        = "pasantias-prod-private"
  description = "Private EC2 - acceso solo desde ELB y bastion"
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
  # Puertos de servicios desde el ELB
  ingress {
    description     = "Frontend desde ELB"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  ingress {
    description     = "Auth service desde ELB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  ingress {
    description     = "Internship service desde ELB"
    from_port       = 8081
    to_port         = 8081
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  ingress {
    description     = "User service desde ELB"
    from_port       = 8083
    to_port         = 8083
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  ingress {
    description     = "Gateway service desde ELB"
    from_port       = 8082
    to_port         = 8082
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  ingress {
    description     = "Linkage service desde ELB"
    from_port       = 8084
    to_port         = 8084
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "pasantias-prod-private" }
}

# ─────────────────────────────────────────
# KEY PAIR — PROD key creada por Terraform
# ─────────────────────────────────────────
resource "aws_key_pair" "prod_key" {
  key_name   = "PROD"
  public_key = file("${path.module}/PROD.pub")
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

# Perfil de instancia IAM de AWS Academy
data "aws_iam_instance_profile" "lab_profile" {
  name = "LabInstanceProfile"
}

# ─────────────────────────────────────────
# BASTION HOST — jump host con IP fija (EIP)
# La IP NO cambia entre sesiones de AWS Academy.
# No es necesario actualizar PROD_BASTION_IP en GitHub Secrets.
# ─────────────────────────────────────────
resource "aws_instance" "bastion" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.micro"
  subnet_id              = aws_subnet.public_1a.id
  key_name               = aws_key_pair.prod_key.key_name
  vpc_security_group_ids = [aws_security_group.sg_bastion.id]
  iam_instance_profile   = data.aws_iam_instance_profile.lab_profile.name
  tags                   = { Name = "pasantias-prod-bastion" }

  user_data = base64encode(<<-EOF
    #!/bin/bash
    set -e
    exec > /var/log/pasantias-bastion-init.log 2>&1

    apt-get update -y
    apt-get install -y curl unzip jq ansible git

    snap install amazon-ssm-agent --classic || true
    systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service || true
    systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service || true

    mkdir -p /home/ubuntu/actions-runner
    cd /home/ubuntu/actions-runner
    curl -o runner.tar.gz -L https://github.com/actions/runner/releases/download/v2.322.0/actions-runner-linux-x64-2.322.0.tar.gz
    tar xzf runner.tar.gz
    rm runner.tar.gz
    chown -R ubuntu:ubuntu /home/ubuntu/actions-runner

    mkdir -p /home/ubuntu/.ssh
    chmod 700 /home/ubuntu/.ssh
    chown ubuntu:ubuntu /home/ubuntu/.ssh

    echo "Bastion init complete"
  EOF
  )

  lifecycle {
    ignore_changes = [user_data, ami]
  }
}

# Elastic IP fija — permanece aunque la instancia se reinicie
resource "aws_eip" "bastion_eip" {
  instance   = aws_instance.bastion.id
  domain     = "vpc"
  depends_on = [aws_internet_gateway.igw]
  tags       = { Name = "pasantias-prod-bastion-eip" }
}

# ─────────────────────────────────────────
# APPLICATION LOAD BALANCER
# ─────────────────────────────────────────
resource "aws_lb" "prod_elb" {
  name               = "pasantias-prod-elb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.sg_elb.id]
  subnets            = [aws_subnet.public_1a.id, aws_subnet.public_1b.id]
  tags               = { Name = "pasantias-prod-elb" }
}

# ─────────────────────────────────────────
# TARGET GROUPS
# ─────────────────────────────────────────
resource "aws_lb_target_group" "frontend_tg" {
  name     = "pasantias-prod-frontend-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-frontend-tg" }
}

resource "aws_lb_target_group" "auth_tg" {
  name     = "pasantias-prod-auth-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-auth-tg" }
}

resource "aws_lb_target_group" "internship_tg" {
  name     = "pasantias-prod-internship-tg"
  port     = 8081
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-internship-tg" }
}

resource "aws_lb_target_group" "user_tg" {
  name     = "pasantias-prod-user-tg"
  port     = 8083
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-user-tg" }
}

resource "aws_lb_target_group" "linkage_tg" {
  name     = "pasantias-prod-linkage-tg"
  port     = 8084
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-linkage-tg" }
}

resource "aws_lb_target_group" "gateway_tg" {
  name     = "pasantias-prod-gateway-tg"
  port     = 8082
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = "/api/users/health"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  tags = { Name = "pasantias-prod-gateway-tg" }
}

# ─────────────────────────────────────────
# ELB LISTENERS — 3 puertos separados
#   :80   → frontend-web
#   :8080 → auth-service
#   :8081 → internship-service
# ─────────────────────────────────────────
resource "aws_lb_listener" "frontend_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend_tg.arn
  }
}

resource "aws_lb_listener" "auth_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8080
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.auth_tg.arn
  }
}

resource "aws_lb_listener" "internship_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8081
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.internship_tg.arn
  }
}

resource "aws_lb_listener" "user_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8083
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.user_tg.arn
  }
}

resource "aws_lb_listener" "linkage_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8084
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.linkage_tg.arn
  }
}

resource "aws_lb_listener" "gateway_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8082
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway_tg.arn
  }
}

# ─────────────────────────────────────────
# EC2 — PROD Services (auth + internship + frontend)
# ─────────────────────────────────────────
resource "aws_instance" "prod_auth_jobs" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.small"
  subnet_id              = aws_subnet.private_1a.id
  key_name               = aws_key_pair.prod_key.key_name
  vpc_security_group_ids = [aws_security_group.sg_private.id]
  iam_instance_profile   = data.aws_iam_instance_profile.lab_profile.name

  user_data = base64encode(<<-EOF
    #!/bin/bash
    set -e
    exec > /var/log/pasantias-ec2-init.log 2>&1

    apt-get update -y
    apt-get install -y ca-certificates curl gnupg git apt-transport-https

    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu noble stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    systemctl enable docker
    systemctl start docker
    usermod -aG docker ubuntu

    echo "EC2 init complete"
  EOF
  )

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  tags = { Name = "pasantias-prod-ec2-services" }

  # Evitar que Terraform destruya y recree la instancia cuando
  # Ansible modifica los contenedores o cuando cambia la AMI
  lifecycle {
    ignore_changes = [user_data, ami]
  }
}

# ─────────────────────────────────────────
# TARGET GROUP ATTACHMENTS
# ─────────────────────────────────────────
resource "aws_lb_target_group_attachment" "frontend_attachment" {
  target_group_arn = aws_lb_target_group.frontend_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 80
}

resource "aws_lb_target_group_attachment" "auth_attachment" {
  target_group_arn = aws_lb_target_group.auth_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8080
}

resource "aws_lb_target_group_attachment" "internship_attachment" {
  target_group_arn = aws_lb_target_group.internship_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8081
}

resource "aws_lb_target_group_attachment" "user_attachment" {
  target_group_arn = aws_lb_target_group.user_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8083
}

resource "aws_lb_target_group_attachment" "linkage_attachment" {
  target_group_arn = aws_lb_target_group.linkage_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8084
}

resource "aws_lb_target_group_attachment" "gateway_attachment" {
  target_group_arn = aws_lb_target_group.gateway_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8082
}

# ─────────────────────────────────────────
# OUTPUTS
# Comandos para obtener los valores:
#   terraform apply -refresh-only
#   terraform output
# ─────────────────────────────────────────
output "bastion_eip" {
  description = "IP pública fija del bastion PROD (usar como PROD_BASTION_IP en GitHub Secrets — no cambia entre sesiones)"
  value       = aws_eip.bastion_eip.public_ip
}

output "elb_dns_name" {
  description = "DNS del Load Balancer (usar en build-args del frontend para VITE_AUTH_URL y VITE_INTERNSHIP_URL)"
  value       = aws_lb.prod_elb.dns_name
}

output "prod_auth_jobs_private_ip" {
  description = "IP privada del EC2 de servicios (usar como PROD_AUTH_JOBS_IP en GitHub Secrets)"
  value       = aws_instance.prod_auth_jobs.private_ip
}
