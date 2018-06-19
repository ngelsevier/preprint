resource aws_iam_role ecr_image_builder {
  name = "${var.product}.ecr_image_builder"
  assume_role_policy = "${var.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment ecr_push_and_pull_policy_on_image_builder_role {
  role = "${aws_iam_role.ecr_image_builder.name}"
  policy_arn = "${aws_iam_policy.ecr_push_and_pull.arn}"
}

resource aws_iam_policy ecr_push_and_pull {
  name = "${var.product}.ecr_push_and_pull"
  path = "/"
  policy = "${data.aws_iam_policy_document.ecr_push_and_pull.json}"
}

data aws_iam_policy_document ecr_push_and_pull {
  statement = {
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability",
      "ecr:PutImage",
      "ecr:InitiateLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:CompleteLayerUpload"
    ]

    resources = [
      "*"
    ]
  }
}