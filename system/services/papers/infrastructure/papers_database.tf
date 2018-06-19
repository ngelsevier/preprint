resource aws_route53_record papers_database {
  zone_id = "${data.terraform_remote_state.vpc.internal_route53_hosted_zone_id}"
  name = "papers-database"
  ttl = "60"
  type = "CNAME"
  records = [
    "${aws_db_instance.papers.address}"
  ]
}

resource aws_db_instance papers {
  allocated_storage = 60
  engine = "postgres"
  engine_version = "9.6.6"
  identifier = "${var.product}-${var.environment}-${var.service_name}"
  instance_class = "db.t2.medium"
  storage_type = "gp2"
  name = "papers"
  password = "postgres"
  username = "postgres"
  port = "${data.terraform_remote_state.vpc.postgres_database_port}"
  publicly_accessible = false
  db_subnet_group_name = "${aws_db_subnet_group.papers.id}"
  apply_immediately = true
  parameter_group_name = "${aws_db_parameter_group.papers.id}"
  skip_final_snapshot = false
  vpc_security_group_ids = [
    "${data.terraform_remote_state.vpc.postgres_database_security_group_id}"
  ]

  lifecycle {
    prevent_destroy = true
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_db_parameter_group papers {
  name = "${var.product}-${var.environment}-${var.service_name}"
  family = "postgres9.6"

  parameter {
    name = "rds.logical_replication"
    value = 1
    apply_method = "pending-reboot"
  }

  parameter {
    name = "max_replication_slots"
    value = "95"
    apply_method = "pending-reboot"
  }

  parameter {
    name = "max_wal_senders"
    value = "100"
    apply_method = "pending-reboot"
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_db_subnet_group papers {
  name = "${var.product}-${var.environment}-${var.service_name}"

  subnet_ids = [
    "${data.terraform_remote_state.vpc.private_subnet_ids}"
  ]

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_lambda_function papers_database_heartbeat {
  function_name = "${var.product}-${var.environment}-${var.service_name}-database-heartbeat"
  handler = "heartbeat.lambda_handler"
  role = "${data.terraform_remote_state.permissions.papers_database_heartbeat_lambda_executor_iam_role_arn}"
  runtime = "python2.7"
  filename = "${path.module}/heartbeat/heartbeat.zip"
  source_code_hash = "${base64sha256(file("${path.module}/heartbeat/heartbeat.zip"))}"
  timeout = "15"
  vpc_config = {
    subnet_ids = [
      "${data.terraform_remote_state.vpc.private_subnet_ids}"]
    security_group_ids = [
      "${data.terraform_remote_state.vpc.postgres_client_security_group_id}",
      "${data.terraform_remote_state.vpc.outbound_http_to_anywhere_security_group_id}"
    ]
  }

  environment {
    variables = {
      ENCRYPTED_DATABASE_PASSWORD = "${var.encrypted_heartbeat_database_user_password}"
    }
  }

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}-database-heartbeat"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_cloudwatch_event_rule papers_database_heartbeat_trigger {
  name = "${var.product}-${var.environment}-${var.service_name}-database-heartbeat-trigger"
  schedule_expression = "rate(1 minute)"
}

resource aws_cloudwatch_event_target papers_database_heartbeat_trigger_target {
  arn = "${aws_lambda_function.papers_database_heartbeat.arn}"
  rule = "${aws_cloudwatch_event_rule.papers_database_heartbeat_trigger.name}"
}

resource aws_lambda_permission papers_database_heartbeat_trigger_permission {
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.papers_database_heartbeat.function_name}"
  principal = "events.amazonaws.com"
  statement_id = "${var.product}-${var.environment}-${var.service_name}-database-heartbeat-trigger-permission"
}