terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }

  # Stores terraform state in S3 so GitHub Actions runners don't lose infrastructure history
  backend "s3" {
    bucket = "ecommerce-tfstate-aliaz" # MAKE SURE to create this S3 bucket manually in AWS console once!
    key    = "state/terraform.tfstate"
    region = "ap-south-1"
  }
}

provider "aws" {
  region = "ap-south-1"
}

# Variable passed safely from GitHub Secrets
variable "db_password" {
  description = "Database administrator password"
  type        = string
  sensitive   = true
}

# 1. VPC and Networking (For Auto-scaling and high availability)
resource "aws_vpc" "ecommerce_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = { Name = "ecommerce-vpc" }
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_subnet" "public_subnet_1" {
  vpc_id                  = aws_vpc.ecommerce_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags = { Name = "ecommerce-public-1" }
}

resource "aws_subnet" "public_subnet_2" {
  vpc_id                  = aws_vpc.ecommerce_vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true
  tags = { Name = "ecommerce-public-2" }
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.ecommerce_vpc.id
  tags   = { Name = "ecommerce-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.ecommerce_vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }
}

resource "aws_route_table_association" "a" {
  subnet_id      = aws_subnet.public_subnet_1.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "b" {
  subnet_id      = aws_subnet.public_subnet_2.id
  route_table_id = aws_route_table.public.id
}

# 2. EKS Cluster IAM Role
resource "aws_iam_role" "eks_cluster" {
  name = "ecommerce-eks-cluster-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "eks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  role       = aws_iam_role.eks_cluster.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

# 3. EKS Cluster
resource "aws_eks_cluster" "ecommerce" {
  name     = "ecommerce-cluster"
  role_arn = aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]
  }

  depends_on = [aws_iam_role_policy_attachment.eks_cluster_policy]
}

# 4. EKS Node Group (Handles automatic crash recovery and pod scaling)
resource "aws_iam_role" "eks_nodes" {
  name = "ecommerce-eks-node-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "nodes_AmazonEKSWorkerNodePolicy" {
  role       = aws_iam_role.eks_nodes.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "nodes_AmazonEKS_CNI_Policy" {
  role       = aws_iam_role.eks_nodes.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

resource "aws_iam_role_policy_attachment" "nodes_AmazonEC2ContainerRegistryReadOnly" {
  role       = aws_iam_role.eks_nodes.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_eks_node_group" "nodes" {
  cluster_name    = aws_eks_cluster.ecommerce.name
  node_group_name = "ecommerce-node-group"
  node_role_arn   = aws_iam_role.eks_nodes.arn
  subnet_ids      = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]

  scaling_config {
    desired_size = 2
    max_size     = 4
    min_size     = 1
  }

  instance_types = ["t3.medium"]

  depends_on = [
    aws_iam_role_policy_attachment.nodes_AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.nodes_AmazonEKS_CNI_Policy,
    aws_iam_role_policy_attachment.nodes_AmazonEC2ContainerRegistryReadOnly,
  ]
}

# 5. RDS MySQL Database (Stores data safely with auto-recovery)
resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "ecommerce-rds-subnet-group"
  subnet_ids = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]
}

resource "aws_security_group" "rds_sg" {
  name   = "ecommerce-rds-sg"
  vpc_id = aws_vpc.ecommerce_vpc.id

  ingress {
    from_port   = 3306
    to_port     = 3306
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

resource "aws_db_instance" "ecommerce_db" {
  identifier             = "ecommerce-db"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  db_name                = "payment_db"
  username               = "admin"
  password               = var.db_password # Dynamic secret from GitHub Actions
  skip_final_snapshot    = true
  publicly_accessible    = true
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
}

# 6. S3 Bucket for Product Images (Unique suffix added)
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket" "product_images" {
  bucket        = "ecommerce-product-images-${random_id.bucket_suffix.hex}"
  force_destroy = true
}

# ------------------------------------------------------------------
# 7. HELM PROVIDER & KUBERNETES MANAGED RESOURCES
# ------------------------------------------------------------------

data "aws_eks_cluster_auth" "cluster_auth" {
  name = aws_eks_cluster.ecommerce.name
}

provider "helm" {
  kubernetes {
    host                   = aws_eks_cluster.ecommerce.endpoint
    cluster_ca_certificate = base64decode(aws_eks_cluster.ecommerce.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.cluster_auth.token
  }
}

# DEPLOY KAFKA VIA TERRAFORM
resource "helm_release" "kafka" {
  name       = "kafka"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "kafka"

  set {
    name  = "replicaCount"
    value = "1"
  }
  set {
    name  = "auth.clientProtocol"
    value = "none"
  }
  set {
    name  = "listeners.client.protocol"
    value = "PLAINTEXT"
  }

  depends_on = [aws_eks_node_group.nodes]
}

# DEPLOY PROMETHEUS & GRAFANA VIA TERRAFORM
resource "helm_release" "prometheus" {
  name             = "monitoring"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  namespace        = "monitoring"
  create_namespace = true

  set {
    name  = "grafana.service.type"
    value = "LoadBalancer"
  }

  depends_on = [aws_eks_node_group.nodes]
}