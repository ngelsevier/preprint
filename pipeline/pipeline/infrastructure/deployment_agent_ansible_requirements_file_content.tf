data template_file deployment_agent_ansible_requirements_file_content {
  template = "${file("${path.module}/files/deployment_agent_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    automater_version = "${var.ansible_role_versions["automater"]}"
    automation_agent_version = "${var.ansible_role_versions["automation_agent"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    aws_api_client_version = "${var.ansible_role_versions["aws_api_client"]}"
    container_factory_version = "${var.ansible_role_versions["container_factory"]}"
    deployer_version = "${var.ansible_role_versions["deployer"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}