resource aws_iam_role frontend_healthcheck_scheduler {
  name = "ssrn.${var.environment}.frontend_healthcheck_scheduler"
  assume_role_policy = "${data.terraform_remote_state.global.lambda_trust_policy_json}"
}

resource aws_iam_role_policy frontend_healthcheck_scheduler_lambda_execution {
  name = "ssrn.${var.environment}.frontend_healthcheck_scheduler_lambda_execution"
  policy = "${data.aws_iam_policy_document.frontend_healthcheck_scheduler_lambda_execution.json}"
  role = "${aws_iam_role.frontend_healthcheck_scheduler.name}"
}

data aws_iam_policy_document frontend_healthcheck_scheduler_lambda_execution {

  statement = {
    effect = "Allow"

    actions = [
      "ec2:CreateNetworkInterface",
      "ec2:DescribeNetworkInterfaces",
      "ec2:DeleteNetworkInterface",
    ]

    resources = [
      "*"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = [
      "arn:aws:logs:*:*:*"
    ]
  }
}