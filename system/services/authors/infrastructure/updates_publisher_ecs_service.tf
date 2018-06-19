resource aws_ecs_service updates_publisher {
  name = "${var.product}-${var.environment}-${var.updates_publisher_name}"
  task_definition = "${aws_ecs_task_definition.updates_publisher.arn}"
  desired_count = 1
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  deployment_maximum_percent = 100
  deployment_minimum_healthy_percent = 0
}