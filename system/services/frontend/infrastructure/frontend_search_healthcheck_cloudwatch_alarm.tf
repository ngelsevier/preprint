resource aws_cloudwatch_metric_alarm frontend-search-healthcheck-alarm {
  actions_enabled = true
  alarm_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  insufficient_data_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  ok_actions = ["${data.terraform_remote_state.vpc.sns_cloudwatch_alarm_topic}"]
  alarm_name = "${var.product}-${var.environment}-${var.service_name}-search-healthcheck-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods =  5
  metric_name = "Errors"
  namespace = "AWS/Lambda"
  period = 60
  statistic = "Sum"
  threshold = 3

  dimensions {
    FunctionName = "${aws_lambda_function.frontend_healthcheck_scheduler.function_name}"
  }
}