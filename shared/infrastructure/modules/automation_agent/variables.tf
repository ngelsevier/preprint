variable agent_name {
  type = "string"
}

variable ami_id {
  type = "string"
}

variable ansible_playbook_file_content_blobs {
  type = "list"
}

variable ansible_requirements_file_content_blobs {
  type = "list"
}

variable contact_details {
  type = "string"
}

variable bastion_accessible_instance_security_group_id {
  type = "string"
}

variable count {
  type = "string"
}

variable default_ssh_key_name {
  type = "string"
}

variable environment {
  type = "string"
}

variable iam_instance_profile_name {
  type = "string"
}

variable instance_type {
  type = "string"
}

variable ntp_access_security_group_id {
  type = "string"
}

variable outbound_http_to_anywhere_security_group_id {
  type = "string"
}

variable private_subnet_ids {
  type = "list"
}

variable product {
  type = "string"
}

variable security_group_id {
  type = "string"
}

variable volume_size_gigabytes {
  type = "string"
  default = 8
}