resource aws_iam_role search_papers_consumer {
  name = "ssrn.${var.environment}.search_papers_consumer"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}


resource aws_iam_role_policy kinesis_record_consumption {
  name = "ssrn.${var.environment}.kinesis_record_consumption"
  policy = "${data.aws_iam_policy_document.kinesis_record_consumption.json}"
  role = "${aws_iam_role.search_papers_consumer.name}"
}

data aws_iam_policy_document kinesis_record_consumption {

  statement = {
    effect = "Allow"

    actions = [
      "kinesis:GetRecords",
      "kinesis:GetShardIterator",
      "kinesis:ListShards"
    ]

    resources = [
      "arn:aws:kinesis:${var.aws_region}:${data.aws_caller_identity.current.account_id}:stream/${format("%s-papers", var.environment)}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "cloudwatch:PutMetricData"
    ]

    resources = [
      "*"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "dynamodb:CreateTable",
      "dynamodb:DescribeTable",
      "dynamodb:Scan",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem"
    ]

    resources = [
      "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/${format("%s-paperConsumer", var.environment)}"
    ]
  }

}