resource aws_launch_configuration ecs_container_instance___ESCAPED_VERSION__ {
  enable_monitoring = true
  name_prefix = "${var.product}.${var.environment}.${var.cluster_name}_cluster_ecs_container_instance___VERSION__."
  iam_instance_profile = "${data.terraform_remote_state.permissions.ecs_container_instance_iam_instance_profile_name}"
  image_id = "${data.aws_ami.container_instance.id}"
  instance_type = "m5.large"
  key_name = "${data.terraform_remote_state.vpc.default_ssh_key_name}"
  security_groups = [
    "${data.terraform_remote_state.vpc.bastion_accessible_instance_security_group_id}",
    "${data.terraform_remote_state.vpc.outbound_http_to_anywhere_security_group_id}",
    "${aws_security_group.ecs_container_instance.id}",
    "${data.terraform_remote_state.vpc.postgres_client_security_group_id}",
    "${data.terraform_remote_state.vpc.ntp_access_security_group_id}",
  ]
  user_data = "${module.ecs_container_instance_user_data.content_blobs[0]}"


  root_block_device {
    delete_on_termination = true
    volume_size = "30"
    volume_type = "gp2"
  }
}

module ecs_container_instance_user_data {
  source = "./modules/user_data_template"

  ansible_requirements_file_content_blobs = "${list(data.template_file.ecs_container_instance_ansible_requirements_file_content.rendered)}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.ecs_container_instance_ansible_playbook_file_content.rendered)}"
  count = 1
}

data template_file ecs_container_instance_ansible_requirements_file_content {
  template = "${file("${path.module}/files/ecs_container_instance_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    aws_ecs_container_instance_version = "${var.ansible_role_versions["aws_ecs_container_instance"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}

data template_file ecs_container_instance_ansible_playbook_file_content {
  template = "${file("${path.module}/files/ecs_container_instance_ansible_playbook.yml.tpl")}"

  vars {
    ecs_cluster_name = "${aws_ecs_cluster.cluster.name}"
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}

resource aws_security_group ecs_container_instance {
  name = "${var.product}.${var.environment}.${var.cluster_name}_cluster_ecs_container_instance"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  ingress {
    from_port = 0
    to_port = 0
    protocol = -1

    security_groups = [
      "${aws_security_group.ecs_service_load_balancer.id}",
    ]
  }

  egress {
    from_port = 80
    to_port = 80
    protocol = "tcp"

    security_groups = [
      "${aws_security_group.internal_service_load_balancer.id}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.${var.cluster_name}_cluster_ecs_container_instance"
    cluster = "${var.cluster_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}