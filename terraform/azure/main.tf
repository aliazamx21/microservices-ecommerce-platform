terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }

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

resource "azurerm_resource_group" "devops_rg" {
  name     = "ecom-devops-rg"
  location = "Central India"
}

resource "azurerm_kubernetes_cluster" "devops_aks" {
  name                = "ecom-devops-aks"
  location            = azurerm_resource_group.devops_rg.location
  resource_group_name = azurerm_resource_group.devops_rg.name
  dns_prefix          = "ecom-devops-aks"
  oidc_issuer_enabled = true

  default_node_pool {
    name       = "devopspool"
    node_count = 2
    vm_size    = "Standard_D2s_v5"
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "DevOps"
    Project     = "ECommerce"
  }
}

output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.devops_aks.name
  description = "The name of the Azure AKS Cluster"
}

output "resource_group_name" {
  value       = azurerm_resource_group.devops_rg.name
  description = "Azure Resource Group Name"
}