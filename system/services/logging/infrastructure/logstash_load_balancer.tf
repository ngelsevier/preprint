resource aws_elb logstash {
  security_groups = [
    "${data.terraform_remote_state.ecs_cluster.ecs_service_load_balancer_security_group_id}",
    "${data.terraform_remote_state.ecs_cluster.internal_service_load_balancer_security_group_id}"
  ]

  subnets = [
    "${data.terraform_remote_state.vpc.private_subnet_ids}"
  ]

  cross_zone_load_balancing = true
  idle_timeout = 300
  internal = true

  listener {
    instance_port = "${var.logstash_port}"
    instance_protocol = "tcp"
    lb_port = 80
    lb_protocol = "tcp"
  }

  health_check {
    healthy_threshold = 2
    unhealthy_threshold = 2
    target = "tcp:${var.logstash_port}"
    interval = 30
    timeout = 10
  }

  tags {
    Name = "${var.product}.${var.environment}.${var.logstash_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}
