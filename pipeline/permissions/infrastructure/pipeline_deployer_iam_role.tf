resource aws_iam_role pipeline_deployer {
  name = "ssrn_pipeline.${var.environment}.pipeline_deployer"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment pipeline_deployment_permissions_on_pipeline_deployer_role {
  role = "${aws_iam_role.pipeline_deployer.name}"
  policy_arn = "${aws_iam_policy.pipeline_deployment.arn}"
}

resource aws_iam_policy pipeline_deployment {
  name = "ssrn_pipeline.${var.environment}.pipeline_deployment"
  path = "/"
  policy = "${data.aws_iam_policy_document.pipeline_deployment.json}"
}

data aws_iam_policy_document pipeline_deployment {
  statement = {
    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}",
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
      "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_key_arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_pipeline_permissions_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:CreateBucket",
      "s3:GetAccelerateConfiguration",
      "s3:GetBucket*",
      "s3:GetLifecycleConfiguration",
      "s3:GetReplicationConfiguration",
      "s3:ListBucket",
      "s3:PutBucketTagging"
    ]

    resources = [
      "arn:aws:s3:::elsevier-ssrn_pipeline-artifacts-${var.environment}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "route53:GetChange"
    ]

    resources = [
      "arn:aws:route53:::change/*"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "iam:PassRole"
    ]

    resources = [
      "${aws_iam_role.iam_user_ssh_public_key_retriever.arn}",
      "${aws_iam_role.pipeline_automation_server.arn}",
      "${aws_iam_role.ci_agent.arn}",
      "${aws_iam_role.production_deployment_agent.arn}",
      "${aws_iam_role.qa_deployment_agent.arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "autoscaling:*",
      "ec2:*",
      "elasticloadbalancing:*",
      "route53:CreateHostedZone",
      "route53:UpdateHostedZoneComment",
      "route53:GetHostedZone",
      "route53:ListResourceRecordSets",
      "route53:ListTagsForResource",
      "route53:ChangeTagsForResource",
      "route53:ChangeResourceRecordSets"
    ]

    resources = [
      "*"
    ]
  }
}