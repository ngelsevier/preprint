variable automation_server_https_port {
  type = "string"
  default = "8154"
}

variable postgres_database_port {
  type = "string"
  default = 5432
}

variable ssh_user {
  type = "string"
  default = "ubuntu"
}

variable ssh_user_home {
  type = "string"
  default = "/home/ubuntu"
}