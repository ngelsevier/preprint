resource aws_iam_role authors_updates_publisher {
  name = "ssrn.${var.environment}.authors_updates_publisher"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}

resource aws_iam_role_policy authors_kinesis_record_publication {
  name = "ssrn.${var.environment}.authors_kinesis_record_publication"
  policy = "${data.aws_iam_policy_document.authors_kinesis_record_publication.json}"
  role = "${aws_iam_role.authors_updates_publisher.name}"
}

data aws_iam_policy_document authors_kinesis_record_publication {

  statement = {
    effect = "Allow"

    actions = [
      "kinesis:DescribeStream",
      "kinesis:PutRecord",
      "kinesis:PutRecords",
    ]

    resources = [
      "arn:aws:kinesis:${var.aws_region}:${data.aws_caller_identity.current.account_id}:stream/${format("%s-author-updates", var.environment)}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "cloudwatch:PutMetricData",
    ]

    resources = [
      "*"
    ]
  }

}

resource aws_iam_role_policy kms_decryption_on_authors_updates_publisher_role {
  name = "ssrn.${var.environment}.kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.authors_updates_publisher.name}"
}
