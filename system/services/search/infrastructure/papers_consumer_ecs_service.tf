resource aws_ecs_service papers_consumer {
  name = "${var.product}-${var.environment}-${var.papers_consumer_name}"
  task_definition = "${aws_ecs_task_definition.papers_consumer.arn}"
  desired_count = 1
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  deployment_maximum_percent = 100
  deployment_minimum_healthy_percent = 0
}