data aws_iam_policy_document ecs_trust_policy {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    principals = {
      type = "Service"
      identifiers = [
        "ecs.amazonaws.com"
      ]
    }
  }
}