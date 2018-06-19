resource aws_lambda_function replicator_job_event_scheduler {
  function_name = "${var.product}-${var.environment}-${var.service_name}-replicator-event-scheduler"
  handler = "job.lambda_handler"
  role = "${data.terraform_remote_state.permissions.papers_replicator_scheduler_iam_role_arn}"
  runtime = "python2.7"
  filename = "${path.module}/event-scheduler/event-scheduler.zip"
  source_code_hash = "${base64sha256(file("${path.module}/event-scheduler/event-scheduler.zip"))}"
  timeout = "70"

  vpc_config = {
    subnet_ids = [
      "${data.terraform_remote_state.vpc.private_subnet_ids}"]
    security_group_ids = [
      "${data.terraform_remote_state.ecs_cluster.internal_service_client_security_group_id}"
    ]
  }

  environment {
    variables = {
      SECONDS_BETWEEN_SCHEDULING_JOBS = "${var.seconds_between_scheduling_replicator_jobs}"
    }
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}-replicator-event-scheduler"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_cloudwatch_event_rule replicator_event_scheduler_trigger {
  name = "${var.product}-${var.environment}-${var.service_name}-replicator-event-scheduler"
  schedule_expression = "rate(1 minute)"
  is_enabled = true
}

resource aws_cloudwatch_event_target replicator_event_scheduler_trigger_target {
  arn = "${aws_lambda_function.replicator_job_event_scheduler.arn}"
  rule = "${aws_cloudwatch_event_rule.replicator_event_scheduler_trigger.name}"
}

resource aws_lambda_permission replicator_event_scheduler_trigger_permission {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.replicator_job_event_scheduler.function_name}"
  principal = "events.amazonaws.com"
  statement_id = "${var.product}-${var.environment}-${var.service_name}-replicator-event-scheduler-trigger-permission"
}