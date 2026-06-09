# Infrastructure and Deployment Management ☁️

This directory contains all the Infrastructure-as-Code (IaC) and deployment automation scripts used to provision AWS environments and orchestrate application deployments.

---

## 📂 Subdirectories Overview

```text
infra/
├── qa/                 # Terraform code for the Quality Assurance environment
├── prod/               # Terraform code for the Production environment
└── ansible/            # Ansible playbooks for server configuration and container setup
```

---

## 🏗️ AWS Cloud Architecture

Our AWS environments follow security best practices by deploying internal application containers in a private subnet, shielded from direct internet access.

```text
               +----------------------------------------------------+
               |                     AWS VPC                        |
               |                                                    |
               |   +------------------+      +------------------+   |
               |   |  Public Subnet   |      |  Private Subnet  |   |
               |   |                  |      |                  |   |
  Internet ===>|==>|  [Bastion Host]  |==SSH>|   [Services]     |   |
               |   |   (t3.micro)     |      |   (t3.small)     |   |
               |   |                  |      |                  |   |
  Internet ===>|==>|  [Load Balancer] |=====>| - auth-service   |   |
  (Prod only)  |   |    (ALB HTTP)    |      | - internship-svc |   |
               |   +------------------+      | - user-service   |   |
               |                             | - linkage-svc    |   |
               |                             | - gateway-svc    |   |
               |                             | - frontend-web   |   |
               |                             +------------------+   |
               +----------------------------------------------------+
```

### Components Provisioned by Terraform:
* **Virtual Private Cloud (VPC)**: Isolated network segment (`10.0.0.0/16`).
* **Subnets**:
  * **Public Subnet**: Houses the Bastion Host (jump host) and Application Load Balancer.
  * **Private Subnet**: Houses the application server instance.
* **NAT Gateway & Internet Gateway**: Provides outbound internet access to the private subnet (required for downloading Docker images) while preventing incoming connections.
* **Security Groups**:
  * `sg_bastion`: Allows inbound SSH (port 22) only.
  * `sg_elb` (Prod): Exposes public web ports (80, 8080, 8081, 8082, 8083, 8084).
  * `sg_private`: Restricts ingress to port 22 (from Bastion) and service ports (VPC internal or from ALB only).
* **EC2 Instances**:
  * **Bastion Host** (`t3.micro`): Single point of access.
  * **Services Instance** (`t3.small`): Runs Docker Engine.

---

## 🚀 Deployment Automation (Ansible)

Once Terraform sets up the network and hardware, Ansible playbooks configure the software and run the dockerized microservices.

### Ansible Playbooks:
* **[`deploy-qa.yml`](/file:///c:/Users/gisse/sistema-pasantias-vinculacion/infra/ansible/deploy-qa.yml)**: Deploys the application containers using the `:qa` image tags.
* **[`deploy-prod.yml`](/file:///c:/Users/gisse/sistema-pasantias-vinculacion/infra/ansible/deploy-prod.yml)**: Deploys the application containers using the `:latest` image tags.

### Playbook Tasks:
1. **Docker Engine Verification**: Checks if Docker is active.
2. **Persistent Directory Creation**: Creates `/var/lib/pasantias` on the host to store SQLite databases.
3. **Database Initialization**: Sets up empty `.db` files (`auth.db`, `internship.db`, `user.db`, `linkage.db`) to prevent Docker from creating directories when mounting volumes.
4. **Docker Network Configuration**: Creates a custom bridge network (`pasantias-net`) allowing microservices to resolve each other by container name.
5. **Image Pulling**: Fetches updated Docker images from Docker Hub.
6. **Container Replacement**: Gracefully stops and removes existing service containers.
7. **Container Launch**: Launches all containers attached to `pasantias-net`:
   * **`auth-service`** (Port 8080)
   * **`internship-service`** (Port 8081)
   * **`user-service`** (Port 8083)
   * **`linkage-service`** (Port 8084)
   * **`gateway-service`** (Port 8082)
   * **`frontend-web`** (Port 80)
8. **Verification**: Queries container execution statuses and prints them to the pipeline logs.

---

## 🛠️ Infrastructure Commands

### Terraform (Provisioning)
To apply or modify the infrastructure, navigate to the environment directory (`infra/qa` or `infra/prod`) and execute:
```bash
# Initialize Terraform
terraform init

# Plan Infrastructure Changes
terraform plan

# Apply Changes
terraform apply
```

### Ansible (Manual Deployment Run)
```bash
ansible-playbook -i <inventory_file.ini> infra/ansible/deploy-qa.yml
```
*(Note: GitHub Actions automates this step using SSH keys and temporary generated inventories).*
