resource aws_lambda_function logging_janitor {
  function_name = "${var.product}-${var.environment}-${var.service_name}-logging-janitor"
  role = "${data.terraform_remote_state.permissions.logging_janitor_lambda_executor_iam_role_arn}"
  runtime = "python2.7"
  handler = "logging-janitor.lambda_handler"
  filename = "${path.module}/logging-janitor/logging-janitor.zip"
  source_code_hash = "${base64sha256(file("${path.module}/logging-janitor/logging-janitor.zip"))}"
  timeout = "15"
  vpc_config = {
    subnet_ids = [
      "${data.terraform_remote_state.vpc.private_subnet_ids}"]
    security_group_ids = [
      "${data.terraform_remote_state.vpc.outbound_http_to_anywhere_security_group_id}"
    ]
  }

  environment {
    variables = {
      THRESHOLD_IN_DAYS = "${var.logging_cleanup_threshold_in_days}"
      ACCESS_LOG_THRESHOLD_IN_DAYS="${var.logging_cleanup_access_log_threshold_in_days}"
      AWS_ES_HOST = "${aws_elasticsearch_domain.logging_index.endpoint}"
    }
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}-logging-janitor"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_cloudwatch_event_rule logging_janitor_trigger {
  name = "${var.product}-${var.environment}-${var.service_name}-logging-janitor-trigger"
  schedule_expression = "rate(1 day)"
}

resource aws_cloudwatch_event_target logging_janitor_trigger_target {
  arn = "${aws_lambda_function.logging_janitor.arn}"
  rule = "${aws_cloudwatch_event_rule.logging_janitor_trigger.name}"
}

resource aws_lambda_permission logging_janitor_trigger_permission {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.logging_janitor.function_name}"
  principal = "events.amazonaws.com"
  statement_id = "${var.product}-${var.environment}-${var.service_name}-logging-janitor-trigger-permission"
}