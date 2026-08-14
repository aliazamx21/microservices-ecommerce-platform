terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }

  backend "gcs" {
    bucket = "ecom-gcp-tfstate-aliaz"
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

# --- EXPLICIT PROJECT SERVICE ENABLEMENT ---
resource "google_project_service" "compute" {
  project            = var.gcp_project_id
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "container" {
  project            = var.gcp_project_id
  service            = "container.googleapis.com"
  disable_on_destroy = false
}

# --- VPC & GKE ---
resource "google_compute_network" "ai_vpc" {
  name                    = "ecom-ai-mcp-network"
  auto_create_subnetworks = true
  depends_on              = [google_project_service.compute]
}

resource "google_container_cluster" "ai_cluster" {
  name     = "ecom-ai-cluster"
  location = "asia-south1"
  network  = google_compute_network.ai_vpc.name

  enable_autopilot = true
  depends_on       = [google_project_service.container]
}

output "kubernetes_cluster_name" {
  value       = google_container_cluster.ai_cluster.name
  description = "The name of the GKE cluster"
}

output "kubernetes_cluster_location" {
  value       = google_container_cluster.ai_cluster.location
  description = "The region of the GKE cluster"
}