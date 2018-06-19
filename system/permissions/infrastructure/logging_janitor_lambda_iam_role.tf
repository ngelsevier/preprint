resource aws_iam_role logging_janitor_lambda_executor {
  name = "ssrn.${var.environment}.logging_janitor_lambda_executor"
  assume_role_policy = "${data.terraform_remote_state.global.lambda_trust_policy_json}"
}

resource aws_iam_role_policy_attachment logging_janitor_lambda_permissions_on_logging_janitor_lambda_executor_role {
  role = "${aws_iam_role.logging_janitor_lambda_executor.name}"
  policy_arn = "${aws_iam_policy.logging_janitor_lambda_execution.arn}"
}

resource aws_iam_policy logging_janitor_lambda_execution {
  name = "ssrn.${var.environment}.logging_janitor_lambda_execution"
  path = "/"
  policy = "${data.aws_iam_policy_document.logging_janitor_lambda_execution.json}"
}


data aws_iam_policy_document logging_janitor_lambda_execution {

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

  statement = {
    effect = "Allow"

    actions = [
      "sts:GetSessionToken"
    ]

    resources = [
      "arn:aws:es:*:*:*"
    ]
  }
}
