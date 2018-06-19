resource aws_cloudwatch_metric_alarm papers-kinesis-stream-get-records-lag-alarm {
  actions_enabled = true
  alarm_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  insufficient_data_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  ok_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  alarm_name = "${var.product}-${var.environment}-${var.service_name}-papers-kinesis-stream-get-records-lag-alarm"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods =  20
  metric_name = "GetRecords.Records"
  namespace = "AWS/Kinesis"
  period = 60
  statistic = "Sum"
  threshold = 0

  dimensions {
    StreamName = "${var.environment}-papers"
  }

  alarm_description = "Triggering of this alarm implies that Papers Consumer is not reading from Kinesis"
}