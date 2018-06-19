variable automation_agent_environments {
  type = "list"

  default = [
    "ci",
    "pipeline"
  ]
}


variable automation_server_http_port {
  type = "string"
  default = "8153"
}

variable automation_server_https_port {
  type = "string"
  default = "8154"
}

variable product {
  type = "string"
  default = "ssrn_pipeline_controller"
}

variable ssh_user {
  type = "string"
  default = "ubuntu"
}

variable ssh_user_home {
  type = "string"
  default = "/home/ubuntu"
}