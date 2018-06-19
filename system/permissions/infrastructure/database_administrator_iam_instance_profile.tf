resource aws_iam_instance_profile database_administrator {
  name = "ssrn.${var.environment}.database-administrator"
  role = "${aws_iam_role.database_administrator.name}"
}

resource aws_iam_role database_administrator {
  name = "ssrn.${var.environment}.database-administrator"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_database_administrator {
  role = "${aws_iam_role.database_administrator.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}
