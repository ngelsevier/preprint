locals {
  # Avoid asking ECS to run a fractional number of instances at any point during a rolling deployment.
  desired_deployment_maximum_percent = 150
}

resource aws_ecs_service logstash {
  name = "${var.product}-${var.environment}-${var.logstash_name}"
  task_definition = "${aws_ecs_task_definition.logstash.arn}"
  desired_count = "${var.logstash_instance_count}"
  cluster = "${data.terraform_remote_state.ecs_cluster.cluster_arn}"
  iam_role = "${data.terraform_remote_state.permissions.ecs_service_scheduler_iam_role_arn}"
  deployment_maximum_percent = "${(local.desired_deployment_maximum_percent * var.logstash_instance_count) % 100 == 0 ? local.desired_deployment_maximum_percent : ceil(local.desired_deployment_maximum_percent / 100.0) * 100}"
  deployment_minimum_healthy_percent = 100

  load_balancer {
    elb_name = "${aws_elb.logstash.name}"
    container_name = "service"
    container_port = "${var.logstash_port}"
  }


}