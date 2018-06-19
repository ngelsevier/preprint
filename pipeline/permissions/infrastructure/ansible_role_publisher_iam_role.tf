resource aws_iam_role ansible_role_publisher {
  name = "ssrn_pipeline.${var.environment}.ansible_role_publisher"
  assume_role_policy = "${data.terraform_remote_state.global.current_aws_account_trust_policy_json}"
}

resource aws_iam_role_policy_attachment ansible_role_publication_permissions_on_ansible_role_publisher_role {
  role = "${aws_iam_role.ansible_role_publisher.name}"
  policy_arn = "${aws_iam_policy.ansible_role_publication.arn}"
}

resource aws_iam_policy ansible_role_publication {
  name = "ssrn_pipeline.${var.environment}.ansible_role_publication"
  path = "/"
  policy = "${data.aws_iam_policy_document.ansible_role_publication.json}"
}

data aws_iam_policy_document ansible_role_publication {

  statement = {
    effect = "Allow"

    actions = [
      "s3:PutObject",
      "s3:PutObjectAcl"
    ]

    resources = [
      "${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_arn}/infrastructure/configuration-management/ansible-roles/*"
    ]
  }

}