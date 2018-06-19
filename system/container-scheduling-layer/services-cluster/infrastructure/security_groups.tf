resource aws_security_group ecs_service_load_balancer {
  name = "${var.product}.${var.environment}.${var.cluster_name}.service_load_balancer"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  tags {
    Name = "${var.product}.${var.environment}.${var.cluster_name}.service_load_balancer"
    cluster = "${var.cluster_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group_rule outbound_to_any_port_on_ecs_container_instance {
  type = "egress"
  from_port = 0
  to_port = 65535
  protocol = "all"
  security_group_id = "${aws_security_group.ecs_service_load_balancer.id}"
  source_security_group_id = "${aws_security_group.ecs_container_instance.id}"
}

resource aws_security_group internal_service_load_balancer {
  name = "${var.product}.${var.environment}.${var.cluster_name}.internal_service_load_balancer"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  tags {
    Name = "${var.product}.${var.environment}.${var.cluster_name}.internal_service_load_balancer"
    cluster = "${var.cluster_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group_rule inbound_http_to_internal_service_load_balancer {
  from_port = 80
  to_port = 80
  protocol = "tcp"
  security_group_id = "${aws_security_group.internal_service_load_balancer.id}"
  type = "ingress"
  source_security_group_id = "${aws_security_group.ecs_container_instance.id}"
}

resource aws_security_group_rule inbound_internal_service_client_to_internal_service_load_balancer {
  from_port = 80
  to_port = 80
  protocol = "tcp"
  security_group_id = "${aws_security_group.internal_service_load_balancer.id}"
  type = "ingress"
  source_security_group_id = "${aws_security_group.internal_service_client.id}"
}

resource aws_security_group internal_service_client {
  name = "${var.product}.${var.environment}.${var.cluster_name}.internal_service_client"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  egress {
    from_port = 80
    to_port = 80
    protocol = "tcp"

    security_groups = [
      "${aws_security_group.internal_service_load_balancer.id}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.${var.cluster_name}.internal_service_client"
    cluster = "${var.cluster_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }

}
