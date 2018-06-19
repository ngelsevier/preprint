resource aws_autoscaling_group database_administrator {
  name = "${aws_launch_configuration.database_administrator.name}"
  max_size = 1
  min_size = 0
  desired_capacity = 0
  launch_configuration = "${aws_launch_configuration.database_administrator.id}"
  health_check_grace_period = 300
  health_check_type = "EC2"

  vpc_zone_identifier = [
    "${module.foundation.private_subnet_ids}"
  ]

  tag {
    key = "Name"
    value = "ssrn.${var.environment}.database-administrator"
    propagate_at_launch = true
  }

  tag {
    key = "contact"
    value = "${var.contact_details}"
    propagate_at_launch = true
  }

  tag {
    key = "environment"
    value = "${var.environment}"
    propagate_at_launch = true
  }

  tag {
    key = "product"
    value = "ssrn"
    propagate_at_launch = true
  }
}

resource aws_launch_configuration database_administrator {
  name_prefix = "ssrn.${var.environment}.database-administrator."
  image_id = "${module.foundation.default_ami_id}"
  instance_type = "t2.nano"
  key_name = "${module.foundation.default_ssh_key_name}"
  iam_instance_profile = "${data.terraform_remote_state.permissions.database_administrator_iam_instance_profile_name}"

  security_groups = [
    "${module.foundation.bastion_accessible_instance_security_group_id}",
    "${module.foundation.outbound_http_to_anywhere_security_group_id}",
    "${aws_security_group.postgres_client.id}",
    "${module.foundation.ntp_access_security_group_id}"
  ]

  user_data = "${module.database_administrator_user_data.content_blobs[0]}"

  lifecycle {
    create_before_destroy = true
  }
}

module database_administrator_user_data {
  source = "./modules/user_data_template"

  ansible_requirements_file_content_blobs = "${list(data.template_file.database_administrator_ansible_requirements_file_content.rendered)}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.database_administrator_ansible_playbook_file_content.rendered)}"
  count = 1
}

data template_file database_administrator_ansible_requirements_file_content {
  template = "${file("${path.module}/files/database_administrator_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    aws_elasticsearch_client_version = "${var.ansible_role_versions["aws_elasticsearch_client"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    database_administrator_version = "${var.ansible_role_versions["database_administrator"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}

data template_file database_administrator_ansible_playbook_file_content {
  template = "${file("${path.module}/files/database_administrator_ansible_playbook.yml.tpl")}"

  vars {
    elasticsearch_endpoint = "${var.elasticsearch_endpoint}"
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}