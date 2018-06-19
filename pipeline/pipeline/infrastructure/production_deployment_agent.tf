module production_deployment_agent {
  source = "./modules/automation_agent"

  agent_name = "production-deployment-agent"
  ami_id = "${module.foundation.default_ami_id}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.production_deployment_agent_ansible_playbook_file_content.rendered)}"
  ansible_requirements_file_content_blobs = "${list(data.template_file.deployment_agent_ansible_requirements_file_content.rendered)}"
  bastion_accessible_instance_security_group_id = "${module.foundation.bastion_accessible_instance_security_group_id}"
  contact_details = "${var.contact_details}"
  count = 1
  default_ssh_key_name = "${module.foundation.default_ssh_key_name}"
  environment = "${var.environment}"
  iam_instance_profile_name = "${data.terraform_remote_state.permissions.production_deployment_agent_iam_instance_profile_name}"
  instance_type = "t2.medium"
  outbound_http_to_anywhere_security_group_id = "${module.foundation.outbound_http_to_anywhere_security_group_id}"
  private_subnet_ids = "${module.foundation.private_subnet_ids}"
  product = "${var.product}"
  security_group_id = "${aws_security_group.automation_agent.id}"
  ntp_access_security_group_id = "${module.foundation.ntp_access_security_group_id}"
}

data template_file production_deployment_agent_ansible_playbook_file_content {
  template = "${file("${path.module}/files/deployment_agent_ansible_playbook.yml.tpl")}"

  vars {
    automation_server_hostname = "${aws_route53_record.automation_server_internal.fqdn}"
    automation_server_https_port = "${var.automation_server_https_port}"
    agent_auto_register_key = "${var.automation_agent_auto_register_key}"
    agent_environments = "${join(",", var.production_deployment_agent_environments)}"
    agent_resources = "deployer-outside-ssrn-vpc,docker"
    agent_root_cert_pem_content = "${var.automation_agent_root_cert_pem_content}"
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}