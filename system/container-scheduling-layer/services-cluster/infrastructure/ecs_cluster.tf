resource aws_ecs_cluster cluster {
  name = "${var.product}-${var.environment}-${var.cluster_name}"
}