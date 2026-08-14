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
}

# Explicitly tell Terraform to ensure the Compute API is active using the provider's token
resource "google_project_service" "compute_api" {
  project            = var.gcp_project_id
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "container_api" {
  project            = var.gcp_project_id
  service            = "container.googleapis.com"
  disable_on_destroy = false
}

resource "google_compute_network" "ai_vpc" {
  name                    = "ecom-ai-mcp-network"
  auto_create_subnetworks = true
  depends_on              = [google_project_service.compute_api]
}

resource "google_container_cluster" "ai_cluster" {
  name             = "ecom-ai-cluster"
  location         = "asia-south1"
  network          = google_compute_network.ai_vpc.name
  enable_autopilot = true
  depends_on       = [google_project_service.container_api]
}