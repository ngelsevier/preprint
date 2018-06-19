resource aws_sns_topic sns_cloudwatch_alarm_topic {
  name = "ssrn-${var.environment}-cloudwatch-alarm-topic"
}