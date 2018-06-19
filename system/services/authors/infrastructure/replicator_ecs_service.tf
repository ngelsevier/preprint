resource aws_ecs_service authors_replicator {
  name = "${var.product}-${var.environment}-${var.replicator_name}"
  task_definition = "${aws_ecs_task_definition.authors_replicator.arn}"
  desired_count = 1
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  iam_role = "${data.terraform_remote_state.permissions.ecs_service_scheduler_iam_role_arn}"
  deployment_maximum_percent = 100
  deployment_minimum_healthy_percent = 0

  load_balancer = {
    target_group_arn = "${aws_alb_target_group.authors_replicator_service.arn}"
    container_name = "service"
    container_port = "8080"
  }
}