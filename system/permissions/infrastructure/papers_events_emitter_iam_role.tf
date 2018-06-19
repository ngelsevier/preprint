resource aws_iam_role papers_emitter {
  name = "ssrn.${var.environment}.papers_emitter"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}

resource aws_iam_role_policy kinesis_record_publication {
  name = "ssrn.${var.environment}.kinesis_record_publication"
  policy = "${data.aws_iam_policy_document.kinesis_record_publication.json}"
  role = "${aws_iam_role.papers_emitter.name}"
}

data aws_iam_policy_document kinesis_record_publication {

  statement = {
    effect = "Allow"

    actions = [
      "kinesis:DescribeStream",
      "kinesis:PutRecord",
      "kinesis:PutRecords",
    ]

    resources = [
      "arn:aws:kinesis:${var.aws_region}:${data.aws_caller_identity.current.account_id}:stream/${format("%s-papers", var.environment)}"
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

resource aws_iam_role_policy kms_decryption_on_papers_emitter_role {
  name = "ssrn.${var.environment}.kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.papers_emitter.name}"
}