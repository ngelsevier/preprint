resource aws_iam_role artifacts_publisher {
  name = "ssrn.${var.environment}.artifacts_publisher"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy artifacts_publication_on_artifacts_publisher {
  name = "ssrn.${var.environment}.artifacts_publication"
  policy = "${data.aws_iam_policy_document.artifacts_publication.json}"
  role = "${aws_iam_role.artifacts_publisher.name}"
}

data aws_iam_policy_document artifacts_publication {

  statement = {
    effect = "Allow"

    actions = [
      "s3:PutObject",
      "s3:PutObjectAcl"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}/artifacts/*"
    ]
  }

}