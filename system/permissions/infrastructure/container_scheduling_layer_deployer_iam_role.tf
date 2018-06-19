resource aws_iam_role container_scheduling_layer_deployer {
  name = "ssrn.${var.environment}.container_scheduling_layer_deployer"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment container_scheduling_layer_deployment_permissions_on_container_scheduling_layer_deployer_role {
  role = "${aws_iam_role.container_scheduling_layer_deployer.name}"
  policy_arn = "${aws_iam_policy.container_scheduling_layer_deployment.arn}"
}

resource aws_iam_policy container_scheduling_layer_deployment {
  name = "ssrn.${var.environment}.container_scheduling_layer_deployment"
  path = "/"
  policy = "${data.aws_iam_policy_document.container_scheduling_layer_deployment.json}"
}

data aws_iam_policy_document container_scheduling_layer_deployment {
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
      "${data.terraform_remote_state.global.ssrn_container_scheduling_layer_services_cluster_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_key_arn}",
      "${data.terraform_remote_state.global.ssrn_permissions_infrastructure_state_s3_key_arns_by_environment[var.environment]}",
      "${data.terraform_remote_state.global.ssrn_vpc_infrastructure_state_s3_key_arns_by_environment[var.environment]}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "iam:PassRole"
    ]

    resources = [
      "${aws_iam_role.ecs_container_instance.arn}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "autoscaling:*",
      "cloudwatch:GetDashboard",
      "cloudwatch:PutDashboard",
      "cloudwatch:ListDashboards",
      "ec2:*",
      "ecs:*"
    ]

    resources = [
      "*"
    ]
  }
}