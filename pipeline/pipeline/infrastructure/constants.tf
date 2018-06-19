variable automation_server_http_port {
  type = "string"
  default = "8153"
}

variable automation_server_https_port {
  type = "string"
  default = "8154"
}

variable ci_agent_environments {
  type = "list"

  default = [
    "ci"
  ]
}

variable production_deployment_agent_environments {
  type = "list"

  default = [
    "production",
    "pipeline-controller"
  ]
}

variable qa_deployment_agent_environments {
  type = "list"

  default = [
    "qa"
  ]
}

variable product {
  type = "string"
  default = "ssrn_pipeline"
}

variable ssh_user {
  type = "string"
  default = "ubuntu"
}

variable ssh_user_home {
  type = "string"
  default = "/home/ubuntu"
}