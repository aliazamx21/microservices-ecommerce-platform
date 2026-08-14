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
  }

  backend "s3" {
    bucket = "ecommerce-tfstate-aliaz"
    key    = "state/terraform.tfstate"
    region = "ap-south-1"
  }
}

provider "aws" {
  region = "ap-south-1"
}

variable "db_password" {
  description = "Database administrator password"
  type        = string
  sensitive   = true
}

# --- VPC & NETWORKING ---
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
  cidr_block              = "10.0.3.0/24"
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

# --- EKS CLUSTER IAM & CLUSTER ---
resource "aws_iam_role" "eks_cluster" {
  name = "ecommerce-eks-cluster-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "eks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  role       = aws_iam_role.eks_cluster.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_eks_cluster" "ecommerce" {
  name     = "ecommerce-cluster"
  role_arn = aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]
  }

  depends_on = [aws_iam_role_policy_attachment.eks_cluster_policy]
}

# --- EKS WORKER NODE IAM & NODE GROUP ---
resource "aws_iam_role" "eks_nodes" {
  name = "ecommerce-eks-node-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
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

  instance_types = ["t3.large"]

  tags = {
    Name = "ecommerce-worker-node"
  }

  depends_on = [
    aws_iam_role_policy_attachment.nodes_AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.nodes_AmazonEKS_CNI_Policy,
    aws_iam_role_policy_attachment.nodes_AmazonEC2ContainerRegistryReadOnly,
  ]
}

# --- RDS MYSQL DATABASE ---
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
    cidr_blocks = [aws_vpc.ecommerce_vpc.cidr_block]
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
  password               = var.db_password
  skip_final_snapshot    = true
  publicly_accessible    = false
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
}

# --- S3 BUCKET FOR ECOMMERCE PRODUCT IMAGES ---
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "aws_s3_bucket" "product_images" {
  bucket        = "ecommerce-product-images-${random_id.bucket_suffix.hex}"
  force_destroy = true
}

# --- OUTPUTS ---
output "rds_endpoint" {
  description = "The endpoint of the RDS MySQL database"
  value       = aws_db_instance.ecommerce_db.endpoint
}

output "s3_bucket_name" {
  description = "The name of the S3 bucket for product images"
  value       = aws_s3_bucket.product_images.bucket
}