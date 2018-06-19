resource aws_iam_role controller_deployer {
  name = "ssrn_pipeline.${var.environment}.controller_deployer"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment controller_deployment_permissions_on_controller_deployer_role {
  role = "${aws_iam_role.controller_deployer.name}"
  policy_arn = "${aws_iam_policy.controller_deployment.arn}"
}

resource aws_iam_policy controller_deployment {
  name = "ssrn_pipeline.${var.environment}.controller_deployment"
  path = "/"
  policy = "${data.aws_iam_policy_document.controller_deployment.json}"
}

data aws_iam_policy_document controller_deployment {
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
      "${data.terraform_remote_state.global.ssrn_pipeline_controller_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
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
      "${aws_iam_role.controller_automation_server.arn}",
      "${aws_iam_role.controller_agent.arn}",
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