resource aws_lambda_function frontend_healthcheck_scheduler {
  function_name = "${var.product}-${var.environment}-${var.service_name}-healthcheck-scheduler"
  handler = "job.lambda_handler"
  role = "${data.terraform_remote_state.permissions.frontend_healthcheck_scheduler_iam_role_arn}"
  runtime = "python2.7"
  filename = "${path.module}/healthcheck-scheduler/healthcheck-scheduler.zip"
  source_code_hash = "${base64sha256(file("${path.module}/healthcheck-scheduler/healthcheck-scheduler.zip"))}"
  timeout = "70"

  environment {
    variables = {
      frontend_healthcheck_lambda_enabled = "${var.frontend_healthcheck_lambda_enabled}"
    }
  }

  vpc_config = {
    subnet_ids = [
      "${data.terraform_remote_state.vpc.private_subnet_ids}"]
    security_group_ids = [
      "${data.terraform_remote_state.vpc.outbound_http_to_anywhere_security_group_id}"
    ]
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}-healthcheck-scheduler"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_cloudwatch_event_rule frontend_healthcheck_scheduler_trigger {
  name = "${var.product}-${var.environment}-${var.service_name}-healthcheck-scheduler"
  schedule_expression = "rate(1 minute)"
  is_enabled = "${var.frontend_healthcheck_lambda_enabled}"
}

resource aws_cloudwatch_event_target frontend_healthcheck_scheduler_trigger_target {
  arn = "${aws_lambda_function.frontend_healthcheck_scheduler.arn}"
  rule = "${aws_cloudwatch_event_rule.frontend_healthcheck_scheduler_trigger.name}"
}

resource aws_lambda_permission frontend_healthcheck_scheduler_trigger_permission {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.frontend_healthcheck_scheduler.function_name}"
  principal = "events.amazonaws.com"
  statement_id = "${var.product}-${var.environment}-${var.service_name}-healthcheck-scheduler-trigger-permission"
}
