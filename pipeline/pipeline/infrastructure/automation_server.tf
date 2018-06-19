resource aws_route53_record automation_server_ui_public {
  zone_id = "${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
  name = "pipeline"
  type = "A"

  alias {
    name = "${aws_elb.automation_server.dns_name}"
    zone_id = "${aws_elb.automation_server.zone_id}"
    evaluate_target_health = false
  }
}

resource aws_route53_record automation_server_public {
  zone_id = "${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
  name = "automation-server"
  type = "A"

  alias {
    name = "${aws_elb.automation_server.dns_name}"
    zone_id = "${aws_elb.automation_server.zone_id}"
    evaluate_target_health = false
  }
}

resource aws_elb automation_server {
  name = "${replace(var.product, "_", "-")}-${var.environment}-server"
  security_groups = [
    "${module.foundation.inbound_https_from_elsevier_security_group_id}",
    "${aws_security_group.inbound_https_from_ssrn_vpc_automation_agent.id}",
    "${aws_security_group.automation_server_load_balancer.id}"
  ]
  subnets = [
    "${module.foundation.public_subnet_ids}"
  ]

  listener {
    instance_port = "${var.automation_server_http_port}"
    instance_protocol = "http"
    lb_port = 443
    lb_protocol = "https"
    ssl_certificate_id = "${data.terraform_remote_state.global.wildcard_ssrn2_com_acm_certificate_arn}"
  }

  listener {
    instance_port = "${var.automation_server_https_port}"
    instance_protocol = "tcp"
    lb_port = "${var.automation_server_https_port}"
    lb_protocol = "tcp"
  }

  cross_zone_load_balancing = true

  instances = [
    "${aws_instance.automation_server.id}"
  ]

  health_check {
    healthy_threshold = 2
    unhealthy_threshold = 2
    target = "HTTP:${var.automation_server_http_port}/go/auth/login"
    interval = 30
    timeout = 5
  }

  tags {
    Name = "${var.product}.${var.environment}.automation-server"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_route53_record automation_server_internal {
  zone_id = "${module.foundation.internal_route53_hosted_zone_id}"
  name = "automation-server"
  type = "A"
  records = [
    "${aws_instance.automation_server.private_ip}"
  ]
  ttl = "60"
}

resource aws_instance automation_server {
  ami = "${module.foundation.default_ami_id}"
  instance_type = "t2.large"
  key_name = "${module.foundation.default_ssh_key_name}"
  vpc_security_group_ids = [
    "${aws_security_group.automation_server_instance.id}",
    "${module.foundation.bastion_accessible_instance_security_group_id}",
    "${module.foundation.outbound_http_to_anywhere_security_group_id}",
    "${module.foundation.ntp_access_security_group_id}"
  ]
  subnet_id = "${module.foundation.private_subnet_ids[0]}"
  user_data = "${module.automation_server_user_data.content_blobs[0]}"
  iam_instance_profile = "${data.terraform_remote_state.permissions.pipeline_automation_server_iam_instance_profile_name}"

  root_block_device {
    volume_size = 20
    volume_type = "gp2"
  }

  tags {
    Name = "${var.product}.${var.environment}.automation-server"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

data template_file automation_server_ansible_requirements_file_content {
  template = "${file("${path.module}/files/automation_server_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    automation_server_version = "${var.ansible_role_versions["automation_server"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}

data template_file automation_server_ansible_playbook_file_content {
  template = "${file("${path.module}/files/automation_server_ansible_playbook.yml.tpl")}"

  vars {
    automation_server_backup_s3_bucket_name = "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_names_by_environment[var.environment]}"
    automation_server_backup_s3_key_prefix = "pipeline-automation-server"
    automation_server_encrypted_keystore_content = "${var.automation_server_encrypted_keystore_content}"
    automation_server_comma_separated_users_list = "${join(",", concat(var.automation_server_users, list(var.build_monitor_login_credentials)))}"
    automation_server_http_port = "${var.automation_server_http_port}"
    automation_server_https_port = "${var.automation_server_https_port}"
    automation_server_kms_region = "${var.aws_region}"
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}

module automation_server_user_data {
  source = "./modules/user_data_template"

  ansible_requirements_file_content_blobs = "${list(data.template_file.automation_server_ansible_requirements_file_content.rendered)}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.automation_server_ansible_playbook_file_content.rendered)}"
  count = 1
}

resource aws_security_group automation_server_load_balancer {
  name = "${var.product}.${var.environment}.automation-server-load-balancer"

  egress {
    from_port = "${var.automation_server_http_port}"
    to_port = "${var.automation_server_http_port}"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.automation_server_instance.id}"
    ]
  }

  egress {
    from_port = "${var.automation_server_https_port}"
    to_port = "${var.automation_server_https_port}"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.automation_server_instance.id}"
    ]
  }

  vpc_id = "${module.foundation.vpc_id}"

  tags {
    Name = "${var.product}.${var.environment}.automation-server-load-balancer"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group automation_server_instance {
  name = "${var.product}.${var.environment}.automation-server-instance"

  vpc_id = "${module.foundation.vpc_id}"

  tags {
    Name = "${var.product}.${var.environment}.automation-server-instance"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group automation_agent {
  name = "${var.product}.${var.environment}.automation-agent"

  vpc_id = "${module.foundation.vpc_id}"

  egress {
    from_port = "${var.automation_server_https_port}"
    to_port = "${var.automation_server_https_port}"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.automation_server_instance.id}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.automation-agent"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group_rule inbound_http_from_automation_server_load_balancer {
  type = "ingress"
  from_port = "${var.automation_server_http_port}"
  to_port = "${var.automation_server_http_port}"
  protocol = "tcp"
  security_group_id = "${aws_security_group.automation_server_instance.id}"
  source_security_group_id = "${aws_security_group.automation_server_load_balancer.id}"
}

resource aws_security_group_rule inbound_https_from_automation_server_load_balancer {
  type = "ingress"
  from_port = "${var.automation_server_https_port}"
  to_port = "${var.automation_server_https_port}"
  protocol = "tcp"
  security_group_id = "${aws_security_group.automation_server_instance.id}"
  source_security_group_id = "${aws_security_group.automation_server_load_balancer.id}"
}

resource aws_security_group_rule inbound_from_automation_agent {
  type = "ingress"
  from_port = "${var.automation_server_https_port}"
  to_port = "${var.automation_server_https_port}"
  protocol = "tcp"
  security_group_id = "${aws_security_group.automation_server_instance.id}"
  source_security_group_id = "${aws_security_group.automation_agent.id}"
}

resource aws_security_group inbound_https_from_ssrn_vpc_automation_agent {
  name = "${var.product}.${var.environment}.inbound_https_from_ssrn_vpc_automation_agent"

  vpc_id = "${module.foundation.vpc_id}"

  ingress {
    from_port = "${var.automation_server_https_port}"
    to_port = "${var.automation_server_https_port}"
    protocol = "tcp"

    cidr_blocks = [
      "${formatlist("%s/32", var.ssrn_vpc_ip_addresses)}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.inbound_https_from_ssrn_vpc_automation_agent"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}