resource aws_iam_role service_deployer {
  name = "ssrn.${var.environment}.service_deployer"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment service_deployment_permissions_on_service_deployer_role {
  role = "${aws_iam_role.service_deployer.name}"
  policy_arn = "${aws_iam_policy.service_deployment.arn}"
}

resource aws_iam_policy service_deployment {
  name = "ssrn.${var.environment}.service_deployment"
  path = "/"
  policy = "${data.aws_iam_policy_document.service_deployment.json}"
}

data aws_iam_policy_document service_deployment {

  statement = {
    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:PutObject",
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_services_authors_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_frontend_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_logging_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_papers_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_search_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_key_arn}",
      "${data.terraform_remote_state.global.ssrn_container_scheduling_layer_services_cluster_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_permissions_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_vpc_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_authors_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_services_papers_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "route53:ChangeResourceRecordSets",
    ]

    resources = [
      "arn:aws:route53:::hostedzone/${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
    ]
  }
  statement = {
    effect = "Allow"

    actions = [
      "route53:ChangeResourceRecordSets",
    ]

    resources = [
      "arn:aws:route53:::hostedzone/${var.vpc_internal_service_hosted_zone_id}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "iam:PassRole"
    ]

    resources = [
      "${aws_iam_role.ecs_service_scheduler.arn}",
      "${aws_iam_role.search_api.arn}",
      "${aws_iam_role.search_author_updates_subscriber.arn}",
      "${aws_iam_role.search_papers_consumer.arn}",
      "${aws_iam_role.authors_database_heartbeat_lambda_executor.arn}",
      "${aws_iam_role.authors_updates_publisher.arn}",
      "${aws_iam_role.authors_replicator.arn}",
      "${aws_iam_role.authors_replicator_scheduler.arn}",
      "${aws_iam_role.papers_database_heartbeat_lambda_executor.arn}",
      "${aws_iam_role.papers_emitter.arn}",
      "${aws_iam_role.papers_replicator.arn}",
      "${aws_iam_role.papers_replicator_scheduler.arn}",
      "${aws_iam_role.frontend_website.arn}",
      "${aws_iam_role.logging_logstash.arn}",
      "${aws_iam_role.logging_janitor_lambda_executor.arn}",
      "${aws_iam_role.frontend_healthcheck_scheduler.arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "kms:Decrypt"
    ]

    resources = [
      "${aws_kms_key.master_encryption_key.arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "cloudwatch:DescribeAlarms",
      "cloudwatch:PutMetricAlarm",
      "cloudwatch:GetDashboard",
      "cloudwatch:PutDashboard",
      "cloudwatch:ListDashboards",
      "ecs:*",
      "ec2:DescribeSecurityGroups",
      "ec2:DescribeSubnets",
      "ec2:DescribeVpcs",
      "elasticloadbalancing:*",
      "es:AddTags",
      "es:CreateElasticsearchDomain",
      "es:DescribeElasticsearchDomain",
      "es:ListTags",
      "es:UpdateElasticsearchDomainConfig",
      "events:DeleteRule",
      "events:DescribeRule",
      "events:DisableRule",
      "events:EnableRule",
      "events:ListTargetsByRule",
      "events:PutRule",
      "events:PutTargets",
      "events:RemoveTargets",
      "kinesis:AddTagsToStream",
      "kinesis:CreateStream",
      "kinesis:DeleteStream",
      "kinesis:DescribeStream",
      "kinesis:GetRecords",
      "kinesis:IncreaseStreamRetentionPeriod",
      "kinesis:UpdateShardCount",
      "lambda:AddPermission",
      "lambda:CreateFunction",
      "lambda:DeleteFunction",
      "lambda:GetPolicy",
      "lambda:GetFunction",
      "lambda:ListVersionsByFunction",
      "lambda:RemovePermission",
      "lambda:UpdateFunctionCode",
      "lambda:UpdateFunctionConfiguration",
      "rds:AddTagsToResource",
      "rds:CreateDBInstance",
      "rds:CreateDBParameterGroup",
      "rds:CreateDBSubnetGroup",
      "rds:DescribeDBInstances",
      "rds:DescribeDBParameters",
      "rds:DescribeDBParameterGroups",
      "rds:DescribeDBSubnetGroups",
      "rds:ListTagsForResource",
      "rds:ModifyDBInstance",
      "rds:ModifyDBParameterGroup",
      "route53:GetChange",
      "route53:GetHostedZone",
      "route53:ListResourceRecordSets",
      "SNS:GetTopicAttributes",
    ]

    resources = [
      "*"
    ]
  }
}