module automation_agent {
  source = "./modules/automation_agent"

  agent_name = "automation-agent"
  ami_id = "${module.foundation.default_ami_id}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.automation_agent_ansible_playbook_file_content.rendered)}"
  ansible_requirements_file_content_blobs = "${list(data.template_file.automation_agent_ansible_requirements_file_content.rendered)}"
  bastion_accessible_instance_security_group_id = "${module.foundation.bastion_accessible_instance_security_group_id}"
  contact_details = "${var.contact_details}"
  count = 1
  default_ssh_key_name = "${module.foundation.default_ssh_key_name}"
  environment = "${var.environment}"
  iam_instance_profile_name = "${data.terraform_remote_state.permissions.controller_agent_iam_instance_profile_name}"
  instance_type = "t2.medium"
  outbound_http_to_anywhere_security_group_id = "${module.foundation.outbound_http_to_anywhere_security_group_id}"
  private_subnet_ids = "${module.foundation.private_subnet_ids}"
  product = "${var.product}"
  security_group_id = "${aws_security_group.automation_agent.id}"
  ntp_access_security_group_id = "${module.foundation.ntp_access_security_group_id}"
}

data template_file automation_agent_ansible_requirements_file_content {
  template = "${file("${path.module}/files/automation_agent_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    automater_version = "${var.ansible_role_versions["automater"]}"
    automation_agent_version = "${var.ansible_role_versions["automation_agent"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    aws_api_client_version = "${var.ansible_role_versions["aws_api_client"]}"
    deployer_version = "${var.ansible_role_versions["deployer"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}

data template_file automation_agent_ansible_playbook_file_content {
  template = "${file("${path.module}/files/automation_agent_ansible_playbook.yml.tpl")}"

  vars {
    automation_server_hostname = "${aws_route53_record.automation_server_internal.fqdn}"
    automation_server_https_port = "${var.automation_server_https_port}"
    agent_auto_register_key = "${var.automation_agent_auto_register_key}"
    agent_environments = "${join(",", var.automation_agent_environments)}"
    agent_resources = ""
    agent_root_cert_pem_content = "${var.automation_agent_root_cert_pem_content}"
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}