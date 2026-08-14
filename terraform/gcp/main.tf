terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    time = {
      source  = "hashicorp/time"
      version = "~> 0.9"
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

# 1. Enable Compute Engine API
resource "google_project_service" "compute_engine" {
  project            = var.gcp_project_id
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

# 2. Enable Container Engine API
resource "google_project_service" "container_engine" {
  project            = var.gcp_project_id
  service            = "container.googleapis.com"
  disable_on_destroy = false
}

# 3. Wait 30 seconds for APIs to propagate
resource "time_sleep" "wait_for_apis" {
  depends_on = [
    google_project_service.compute_engine,
    google_project_service.container_engine
  ]
  create_duration = "30s"
}

# 4. Create Network after APIs are enabled and propagated
resource "google_compute_network" "ai_vpc" {
  name                    = "ecom-ai-mcp-network"
  auto_create_subnetworks = true
  depends_on              = [time_sleep.wait_for_apis]
}

# 5. Create GKE Cluster
resource "google_container_cluster" "ai_cluster" {
  name             = "ecom-ai-cluster"
  location         = "asia-south1"
  network          = google_compute_network.ai_vpc.name
  enable_autopilot = true
  depends_on       = [google_compute_network.ai_vpc]
}

output "kubernetes_cluster_name" {
  value       = google_container_cluster.ai_cluster.name
  description = "The name of the GKE cluster"
}

output "kubernetes_cluster_location" {
  value       = google_container_cluster.ai_cluster.location
  description = "The region of the GKE cluster"
}