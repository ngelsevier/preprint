resource aws_iam_role ecr_image_consumer {
  name = "${var.product}.ecr_image_consumer"
  assume_role_policy = "${var.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy ecr_pull_policy_on_image_consumer_role {
  name = "${var.product}.ecr_pull"
  role = "${aws_iam_role.ecr_image_consumer.name}"
  policy = "${data.aws_iam_policy_document.ecr_pull.json}"
}

data aws_iam_policy_document ecr_pull {
  statement = {
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability"
    ]

    resources = [
      "*"
    ]
  }
}