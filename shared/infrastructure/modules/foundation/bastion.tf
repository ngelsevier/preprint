resource aws_security_group inbound_ssh_from_elsevier {
  name = "${var.product}.${var.environment}.inbound-ssh-from-elsevier"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"

    cidr_blocks = [
      "${var.elsevier_cidrs}",
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.inbound-ssh-from-elsevier"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group bastion_load_balancer {
  name = "${var.product}.${var.environment}.bastion.load-balancer"
  vpc_id = "${aws_vpc.vpc.id}"

  tags {
    Name = "${var.product}.${var.environment}.bastion.load-balancer"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group_rule outbound_ssh_to_bastion_instance {
  type = "egress"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  security_group_id = "${aws_security_group.bastion_load_balancer.id}"
  source_security_group_id = "${aws_security_group.bastion_instance.id}"
}

resource aws_security_group bastion_instance {
  name = "${var.product}.${var.environment}.bastion.instance"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"

    security_groups = [
      "${aws_security_group.bastion_load_balancer.id}",
    ]
  }

  egress {
    from_port = 22
    to_port = 22
    protocol = "tcp"

    security_groups = [
      "${aws_security_group.bastion_accessible_instance.id}",
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.bastion.instance"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group bastion_accessible_instance {
  name = "${var.product}.${var.environment}.bastion-accessible-instance"
  vpc_id = "${aws_vpc.vpc.id}"

  tags {
    Name = "${var.product}.${var.environment}.bastion-accessible-instance"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group_rule inbound_ssh_from_bastion_instance {
  type = "ingress"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  security_group_id = "${aws_security_group.bastion_accessible_instance.id}"
  source_security_group_id = "${aws_security_group.bastion_instance.id}"
}

resource aws_launch_configuration bastion {
  name_prefix = "${var.product}.${var.environment}.bastion."
  image_id = "${data.aws_ami.default.id}"
  instance_type = "t2.nano"
  key_name = "${aws_key_pair.default.key_name}"
  iam_instance_profile = "${var.bastion_iam_instance_profile}"

  security_groups = [
    "${aws_security_group.bastion_instance.id}",
    "${aws_security_group.outbound_http_to_anywhere.id}",
    "${aws_security_group.ntp_access.id}"
  ]

  user_data = "${module.bastion_user_data.content_blobs[0]}"

  lifecycle {
    create_before_destroy = true
  }
}

data template_file bastion_ansible_requirements_file_content {
  template = "${file("${path.module}/files/bastion_ansible_requirements.yml.tpl")}"

  vars {
    ansible_role_repository_base_url = "https://${var.ansible_role_repository_s3_bucket_domain_name}/infrastructure/configuration-management/ansible-roles"
    aptitude_package_recipient_version = "${var.ansible_role_versions["aptitude_package_recipient"]}"
    aws_ssh_server_version = "${var.ansible_role_versions["aws_ssh_server"]}"
    long_running_host_version = "${var.ansible_role_versions["long_running_host"]}"
    clock_synchronization_host_version = "${var.ansible_role_versions["clock_synchronization_host"]}"
  }
}

data template_file bastion_ansible_playbook_file_content {
  template = "${file("${path.module}/files/bastion_ansible_playbook.yml.tpl")}"

  vars {
    ssh_user = "${var.ssh_user}"
    ssh_user_home = "${var.ssh_user_home}"
  }
}

module bastion_user_data {
  source = "./modules/user_data_template"

  ansible_requirements_file_content_blobs = "${list(data.template_file.bastion_ansible_requirements_file_content.rendered)}"
  ansible_playbook_file_content_blobs = "${list(data.template_file.bastion_ansible_playbook_file_content.rendered)}"
  count = 1
}

resource aws_elb bastion {
  security_groups = [
    "${aws_security_group.inbound_ssh_from_elsevier.id}",
    "${aws_security_group.bastion_load_balancer.id}",
  ]

  subnets = [
    "${aws_subnet.public.*.id}",
  ]

  cross_zone_load_balancing = true
  idle_timeout = 300

  listener {
    instance_port = 22
    instance_protocol = "tcp"
    lb_port = 22
    lb_protocol = "tcp"
  }

  health_check {
    healthy_threshold = 2
    unhealthy_threshold = 2
    target = "tcp:22"
    interval = 30
    timeout = 10
  }

  tags {
    Name = "${var.product}.${var.environment}.bastion"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_autoscaling_group bastion {
  name = "${aws_launch_configuration.bastion.name}"
  max_size = "${length(var.availability_zones)}"
  min_size = "${length(var.availability_zones)}"
  launch_configuration = "${aws_launch_configuration.bastion.id}"
  health_check_grace_period = 300
  health_check_type = "ELB"

  load_balancers = [
    "${aws_elb.bastion.name}",
  ]

  vpc_zone_identifier = [
    "${aws_subnet.private.*.id}",
  ]

  wait_for_elb_capacity = "${length(var.availability_zones)}"

  tag {
    key = "Name"
    value = "${var.product}.${var.environment}.bastion"
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
    value = "${var.product}"
    propagate_at_launch = true
  }
}

resource aws_route53_record bastion {
  zone_id = "${var.route_53_public_hosted_zone_id}"
  name = "bastion.${var.environment}.${var.product}"
  type = "A"

  alias {
    name = "${aws_elb.bastion.dns_name}"
    zone_id = "${aws_elb.bastion.zone_id}"
    evaluate_target_health = true
  }
}