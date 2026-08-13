terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }

  # Remote GCS backend to preserve Terraform state
  backend "gcs" {
    bucket = "ecom-gcp-tfstate-aliaz" # Ensure this GCS bucket exists in GCP asia-south1
    prefix = "terraform/state"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = "asia-south1"
}

variable "gcp_project_id" {
  type        = string
  description = "The ID of your Google Cloud Project"
}

# 1. Dedicated VPC for AI MCP Services
resource "google_compute_network" "ai_vpc" {
  name                    = "ecom-ai-mcp-network"
  auto_create_subnetworks = true
}

# 2. GKE Autopilot Cluster
resource "google_container_cluster" "ai_cluster" {
  name     = "ecom-ai-cluster"
  location = "asia-south1"
  network  = google_compute_network.ai_vpc.name

  enable_autopilot = true
}

# --- OUTPUTS ---
output "kubernetes_cluster_name" {
  value       = google_container_cluster.ai_cluster.name
  description = "The name of the GKE cluster"
}

output "kubernetes_cluster_location" {
  value       = google_container_cluster.ai_cluster.location
  description = "The region of the GKE cluster"
}