variable ansible_role_versions {
  type = "map"
}

variable automation_agent_auto_register_key {
  type = "string"
}

variable automation_agent_root_cert_pem_content {
  type = "string"
}

variable availability_zones {
  type = "list"
}

variable ci_agent_count {
  type = "string"
}

variable ci_agent_resources {
  type = "list"
}

variable aws_region {
  type = "string"
}

variable contact_details {
  type = "string"
}

variable elsevier_cidrs {
  type = "list"
}

variable environment {
  type = "string"
}

variable global_remote_state_s3_bucket {
  type = "string"
}

variable global_remote_state_s3_key {
  type = "string"
}

variable global_remote_state_s3_region {
  type = "string"
}

variable public_ssh_key {
  type = "string"
}

variable automation_server_encrypted_keystore_content {
  type = "string"
}

variable "build_monitor_login_credentials" {
  type = "string"
}

variable automation_server_users {
  type = "list"
  default = [
    "automated_backup_job",
    "HARRISM",
    "HILTONM",
    "NGR",
    "SMITHSTA",
    "CHIKHALIKART",
    "WEBB4"
  ]
}

variable ssrn_vpc_ip_addresses {
  type = "list"
}

variable vpc_cidr {
  type = "string"
}
