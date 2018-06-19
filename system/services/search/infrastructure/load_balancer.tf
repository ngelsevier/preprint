resource aws_alb service {
  name = "${var.product}-${var.environment}-${var.api_name}"
  internal = true
  security_groups = [
    "${data.terraform_remote_state.ecs_cluster.ecs_service_load_balancer_security_group_id}",
    "${data.terraform_remote_state.ecs_cluster.internal_service_load_balancer_security_group_id}"
  ]
  subnets = [
    "${data.terraform_remote_state.vpc.private_subnet_ids}"
  ]

  tags {
    Name = "${var.product}.${var.environment}.${var.api_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_alb_listener service {
  load_balancer_arn = "${aws_alb.service.arn}"
  port = 80
  protocol = "HTTP"

  default_action {
    target_group_arn = "${aws_alb_target_group.service.arn}"
    type = "forward"
  }
}

resource aws_alb_target_group service {
  name = "${var.product}-${var.environment}-${var.api_name}"
  port = 80
  protocol = "HTTP"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"
  deregistration_delay = 30

  health_check {
    interval = 10
    path = "/healthcheck"
    port = "traffic-port"
    protocol = "HTTP"
    timeout = 5
    healthy_threshold = 2
    unhealthy_threshold = 2
    matcher = 200
  }

  tags {
    Name = "${var.product}.${var.environment}.${var.api_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}
