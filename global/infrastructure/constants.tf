variable ssrn_environments {
  type = "list"
  default = [
    "development",
    "qa",
    "staging",
    "production"
  ]
}

variable ssrn_pipeline_environments {
  type = "list"
  default = [
    "production"
  ]
}

variable ssrn_infrastructure_state_s3_keys {
  type = "map"

  default = {
    permissions = "permissions.tfstate"
    vpc = "vpc.tfstate"
    container_scheduling_layer_services_cluster = "container-scheduling-layer/services-cluster.tfstate"
    services_authors = "services/authors.tfstate"
    services_frontend = "services/frontend.tfstate"
    services_search = "services/search.tfstate"
    services_papers = "services/papers.tfstate"
    services_logging = "services/logging.tfstate"
  }
}

variable ssrn_pipeline_infrastructure_state_s3_keys {
  type = "map"

  default = {
    permissions = "permissions.tfstate"
    pipeline = "pipeline.tfstate"
    controller = "controller.tfstate"
  }
}