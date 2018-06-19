module ssrn_ecr_image_consumer_iam_role {
  source = "./modules/aws_ecr_image_consumer_iam_role"
  product = "ssrn"
  current_aws_account_trust_policy_json = "${data.aws_iam_policy_document.current_aws_account_trust_policy.json}"
}

data aws_iam_policy_document ssrn_ecr_image_consumer_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${module.ssrn_ecr_image_consumer_iam_role.iam_role_arn}"
    ]
  }
}