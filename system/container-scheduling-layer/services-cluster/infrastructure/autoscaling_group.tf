resource aws_autoscaling_group ecs_container_instances___ESCAPED_VERSION__ {
  name = "${aws_launch_configuration.ecs_container_instance___ESCAPED_VERSION__.name}"
  max_size = "${length(data.terraform_remote_state.vpc.private_subnet_ids) * var.instances_per_subnet}"
  min_size = "${length(data.terraform_remote_state.vpc.private_subnet_ids) * var.instances_per_subnet}"
  launch_configuration = "${aws_launch_configuration.ecs_container_instance___ESCAPED_VERSION__.name}"
  health_check_grace_period = 300
  health_check_type = "EC2"
  vpc_zone_identifier = [
    "${data.terraform_remote_state.vpc.private_subnet_ids}"
  ]
  wait_for_capacity_timeout = "10m"

  tag {
    propagate_at_launch = true
    key = "Name"
    value = "${var.product}.${var.environment}.${var.cluster_name}_cluster_ecs_container_instance"
  }

  tag {
    propagate_at_launch = true
    key = "cluster"
    value = "${var.cluster_name}"
  }

  tag {
    propagate_at_launch = true
    key = "contact"
    value = "${var.contact_details}"
  }

  tag {
    propagate_at_launch = true
    key = "environment"
    value = "${var.environment}"
  }

  tag {
    propagate_at_launch = true
    key = "product"
    value = "${var.product}"
  }

  tag {
    propagate_at_launch = true
    key = "version"
    value = "__VERSION__"
  }
}