resource aws_ecs_service author_updates_subscriber_service {
  name = "${var.product}-${var.environment}-${var.author_updates_subscriber_name}"
  task_definition = "${aws_ecs_task_definition.author_updates_subscriber_service.arn}"
  desired_count = 1
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  deployment_maximum_percent = 100
  deployment_minimum_healthy_percent = 0
}