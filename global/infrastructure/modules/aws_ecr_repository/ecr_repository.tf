resource aws_ecr_repository repository {
  name = "${var.product}/${var.image_name}"

  lifecycle {
    prevent_destroy = true
  }
}

resource aws_ecr_repository_policy policy {
  repository = "${aws_ecr_repository.repository.name}"
  policy = "${data.aws_iam_policy_document.ecr_push_and_pull.json}"
}

data aws_iam_policy_document ecr_push_and_pull {
  statement = {
    effect = "Allow"

    principals = {
      type = "AWS"
      identifiers = [
        "${var.ecr_push_and_pull_role_arn}"
      ]
    }

    actions = [
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "ecr:BatchCheckLayerAvailability",
      "ecr:PutImage",
      "ecr:InitiateLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:CompleteLayerUpload"
    ]
  }
}