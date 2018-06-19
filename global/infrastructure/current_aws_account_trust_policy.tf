data aws_iam_policy_document current_aws_account_trust_policy {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    principals = {
      type = "AWS"
      identifiers = [
        "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
      ]
    }
  }
}

data "aws_caller_identity" "current" {
}