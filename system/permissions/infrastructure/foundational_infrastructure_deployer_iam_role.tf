resource aws_iam_role foundational_infrastructure_deployer {
  name = "ssrn.${var.environment}.foundational_infrastructure_deployer"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment foundational_infrastructure_deployment_permissions_on_foundational_infrastructure_deployer_role {
  role = "${aws_iam_role.foundational_infrastructure_deployer.name}"
  policy_arn = "${aws_iam_policy.foundational_infrastructure_deployment.arn}"
}

resource aws_iam_policy foundational_infrastructure_deployment {
  name = "ssrn.${var.environment}.foundational_infrastructure_deployment"
  path = "/"
  policy = "${data.aws_iam_policy_document.foundational_infrastructure_deployment.json}"
}

data aws_iam_policy_document foundational_infrastructure_deployment {
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
      "${data.terraform_remote_state.global.ssrn_vpc_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "iam:PassRole"
    ]

    resources = [
      "${aws_iam_role.iam_user_ssh_public_key_retriever.arn}",
      "${aws_iam_role.deployment_agent.arn}",
      "${aws_iam_role.database_administrator.arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_key_arn}",
      "${data.terraform_remote_state.global.ssrn_permissions_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "route53:GetHostedZone",
      "route53:ChangeResourceRecordSets",
      "route53:ListResourceRecordSets"
    ]

    resources = [
      "arn:aws:route53:::hostedzone/${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
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
      "autoscaling:*",
      "ec2:*",
      "elasticloadbalancing:*",
      "route53:CreateHostedZone",
      "route53:UpdateHostedZoneComment",
      "route53:GetHostedZone",
      "route53:ListResourceRecordSets",
      "route53:ListTagsForResource",
      "route53:ChangeTagsForResource",
      "SNS:CreateTopic",
      "SNS:GetTopicAttributes",
    ]

    resources = [
      "*"
    ]
  }
}