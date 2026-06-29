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
    bucket       = "estado-pasantias-gisse-2026-prod"
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

# ELB: puertos públicos — frontend, gateway y todos los microservicios
resource "aws_security_group" "sg_elb" {
  name        = "pasantias-prod-elb"
  description = "Application Load Balancer - todos los servicios"
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
    description = "Gateway service"
    from_port   = 8082
    to_port     = 8082
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
    description     = "Gateway service desde ELB"
    from_port       = 8082
    to_port         = 8082
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
    description     = "Linkage service desde ELB"
    from_port       = 8084
    to_port         = 8084
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_elb.id]
  }
  # Acceso público a la UI y webhooks de n8n
  ingress {
    description = "n8n UI and Webhooks"
    from_port   = 5678
    to_port     = 5678
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
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
# KEY PAIR — PROD key ya creada en AWS
# ─────────────────────────────────────────
data "aws_key_pair" "prod_key" {
  key_name = "PROD"
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
# BASTION HOST — jump host con IP fija (EIP)
# La IP NO cambia entre sesiones de AWS Academy.
# No es necesario actualizar PROD_BASTION_IP en GitHub Secrets.
# ─────────────────────────────────────────
resource "aws_instance" "bastion" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.micro"
  subnet_id              = aws_subnet.public_1a.id
  key_name               = data.aws_key_pair.prod_key.key_name
  vpc_security_group_ids = [aws_security_group.sg_bastion.id]
  tags                   = { Name = "pasantias-prod-bastion" }

  lifecycle {
    ignore_changes = [ami]
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

# ─────────────────────────────────────────
# ELB LISTENERS
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

resource "aws_lb_listener" "gateway_listener" {
  load_balancer_arn = aws_lb.prod_elb.arn
  port              = 8082
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway_tg.arn
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

# ─────────────────────────────────────────
# EC2 — PROD Services (todos los microservicios)
# Subido de t3.small a t3.large para soportar
# kafka + zookeeper + mongo + rabbitmq + neo4j
# + 12 microservicios Java/Python
# ─────────────────────────────────────────
resource "aws_instance" "prod_auth_jobs" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.large"
  subnet_id              = aws_subnet.private_1a.id
  key_name               = data.aws_key_pair.prod_key.key_name
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
    volume_size = 30
    volume_type = "gp3"
  }

  tags = { Name = "pasantias-prod-ec2-services" }

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

resource "aws_lb_target_group_attachment" "gateway_attachment" {
  target_group_arn = aws_lb_target_group.gateway_tg.arn
  target_id        = aws_instance.prod_auth_jobs.id
  port             = 8082
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

# ─────────────────────────────────────────
# S3 BUCKET — para document-service PROD
# ─────────────────────────────────────────
resource "aws_s3_bucket" "documents_prod" {
  bucket        = "pasantias-documents-prod"
  force_destroy = true
  tags          = { Name = "pasantias-documents-prod" }
}

resource "aws_s3_bucket_ownership_controls" "documents_prod" {
  bucket = aws_s3_bucket.documents_prod.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "documents_prod" {
  depends_on = [aws_s3_bucket_ownership_controls.documents_prod]
  bucket     = aws_s3_bucket.documents_prod.id
  acl        = "private"
}

# ─────────────────────────────────────────
# AUTO SCALING POLICIES — CPU based
# ─────────────────────────────────────────
resource "aws_autoscaling_policy" "scale_up" {
  name                   = "pasantias-prod-scale-up"
  autoscaling_group_name = aws_autoscaling_group.prod_asg.name
  adjustment_type        = "ChangeInCapacity"
  scaling_adjustment     = 1
  cooldown               = 120
}

resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "pasantias-prod-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 70
  alarm_description   = "Scale up cuando CPU > 70% por 2 minutes"
  alarm_actions       = [aws_autoscaling_policy.scale_up.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.prod_asg.name
  }
}

resource "aws_autoscaling_policy" "scale_down" {
  name                   = "pasantias-prod-scale-down"
  autoscaling_group_name = aws_autoscaling_group.prod_asg.name
  adjustment_type        = "ChangeInCapacity"
  scaling_adjustment     = -1
  cooldown               = 300
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "pasantias-prod-cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 3
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 20
  alarm_description   = "Scale down cuando CPU < 20% por 3 minutos"
  alarm_actions       = [aws_autoscaling_policy.scale_down.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.prod_asg.name
  }
}

# ─────────────────────────────────────────
# OUTPUTS
# Comandos para obtener los valores:
#   terraform apply -refresh-only
#   terraform output
# ─────────────────────────────────────────
output "bastion_eip" {
  description = "IP pública fija del bastion PROD — usar como PROD_BASTION_IP en GitHub Secrets (no cambia entre sesiones)"
  value       = aws_eip.bastion_eip.public_ip
}

output "elb_dns_name" {
  description = "DNS del Load Balancer PROD — usar en VITE_GATEWAY_URL del frontend si aplica"
  value       = aws_lb.prod_elb.dns_name
}

output "prod_auth_jobs_private_ip" {
  description = "IP privada del EC2 de servicios — usar como PROD_AUTH_JOBS_IP en GitHub Secrets"
  value       = aws_instance.prod_auth_jobs.private_ip
}

output "documents_bucket_prod" {
  description = "Nombre del bucket S3 para document-service PROD"
  value       = aws_s3_bucket.documents_prod.bucket
}

# ─────────────────────────────────────────
# OUTPUTS — agregar al final
# ─────────────────────────────────────────
output "asg_name" {
  description = "Nombre del Auto Scaling Group para verificar en AWS Console"
  value       = aws_autoscaling_group.prod_asg.name
}
