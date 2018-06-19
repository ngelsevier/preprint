resource aws_ecs_service emitter {
  name = "${var.product}-${var.environment}-${var.emitter_name}"
  task_definition = "${aws_ecs_task_definition.emitter.arn}"
  desired_count = 1
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  deployment_maximum_percent = 100
  deployment_minimum_healthy_percent = 0
}