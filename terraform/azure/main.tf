terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }

  # Remote Azure Blob Storage backend for state
  backend "azurerm" {
    resource_group_name  = "ecom-tfstate-rg"
    storage_account_name = "ecomazuretstate"
    container_name       = "tfstate"
    key                  = "devops-tools.terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}

# 1. Resource Group
resource "azurerm_resource_group" "devops_rg" {
  name     = "ecom-devops-rg"
  location = "Central India"
}

# 2. Azure Kubernetes Service (AKS) for DevOps Tools
resource "azurerm_kubernetes_cluster" "devops_aks" {
  name                = "ecom-devops-aks"
  location            = azurerm_resource_group.devops_rg.location
  resource_group_name = azurerm_resource_group.devops_rg.name
  dns_prefix          = "ecom-devops-aks"

  default_node_pool {
    name       = "devopspool"
    node_count = 2
    vm_size    = "Standard_B2s" # UPDATED FROM Standard_D2s_v3 TO MATCH REGIONAL SUBSCRIPTION QUOTA
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "DevOps"
    Project     = "ECommerce"
  }
}

# --- OUTPUTS ---
output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.devops_aks.name
  description = "The name of the Azure AKS Cluster"
}

output "resource_group_name" {
  value       = azurerm_resource_group.devops_rg.name
  description = "Azure Resource Group Name"
}