locals {
  # Avoid asking ECS to run a fractional number of instances at any point during a rolling deployment.
  desired_deployment_maximum_percent = 150
}

resource aws_ecs_service website {
  name = "${var.product}-${var.environment}-${var.website_name}"
  task_definition = "${aws_ecs_task_definition.website.arn}"
  desired_count = "${var.instance_count}"
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  iam_role = "${data.terraform_remote_state.permissions.ecs_service_scheduler_iam_role_arn}"
  deployment_maximum_percent = "${(local.desired_deployment_maximum_percent * var.instance_count) % 100 == 0 ? local.desired_deployment_maximum_percent : ceil(local.desired_deployment_maximum_percent / 100.0) * 100}"
  deployment_minimum_healthy_percent = 100

  load_balancer = {
    target_group_arn = "${aws_alb_target_group.service.arn}"
    container_name = "service"
    container_port = "8080"
  }
}