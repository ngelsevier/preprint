resource aws_cloudwatch_metric_alarm authors-database-oldest-replication-slot-lag-alarm {
  actions_enabled = true
  alarm_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  insufficient_data_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  ok_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  alarm_name = "${var.product}-${var.environment}-${var.service_name}-database-oldest-replication-slot-lag-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods =  1
  metric_name = "OldestReplicationSlotLag"
  namespace = "AWS/RDS"
  period = 300
  statistic = "Maximum"
  threshold = 1073741824

  dimensions {
    DBInstanceIdentifier = "${var.product}-${var.environment}-${aws_db_instance.authors.name}"
  }
}