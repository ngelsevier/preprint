resource aws_iam_role papers_database_heartbeat_lambda_executor {
  name = "ssrn.${var.environment}.papers_database_heartbeat_lambda_executor"
  assume_role_policy = "${data.terraform_remote_state.global.lambda_trust_policy_json}"
}

resource aws_iam_role_policy_attachment papers_database_heartbeat_lambda_permissions_on_papers_database_heartbeat_lambda_executor_role {
  role = "${aws_iam_role.papers_database_heartbeat_lambda_executor.name}"
  policy_arn = "${aws_iam_policy.papers_database_heartbeat_lambda_execution.arn}"
}

resource aws_iam_policy papers_database_heartbeat_lambda_execution {
  name = "ssrn.${var.environment}.papers_database_heartbeat_lambda_execution"
  path = "/"
  policy = "${data.aws_iam_policy_document.papers_database_heartbeat_lambda_execution.json}"
}

data aws_iam_policy_document papers_database_heartbeat_lambda_execution {

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

resource aws_iam_role_policy kms_decryption_policy_on_papers_database_heartbeat_lambda_executor_role {
  name = "ssrn.${var.environment}.kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.papers_database_heartbeat_lambda_executor.name}"
}