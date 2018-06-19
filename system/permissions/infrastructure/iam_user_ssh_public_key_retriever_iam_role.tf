resource aws_iam_role iam_user_ssh_public_key_retriever {
  name = "ssrn.${var.environment}.iam-user-ssh-public-key-retriever"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_policy_on_iam_user_ssh_public_key_retriever {
  role = "${aws_iam_role.iam_user_ssh_public_key_retriever.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_policy iam_user_ssh_public_key_retrieval {
  name = "ssrn.${var.environment}.iam_user_ssh_public_key_retrieval"
  path = "/"
  policy = "${data.aws_iam_policy_document.iam_user_ssh_public_key_retrieval.json}"
}

data aws_iam_policy_document iam_user_ssh_public_key_retrieval {
  statement = {
    effect = "Allow"

    actions = [
      "ec2:DescribeVpcs",
      "ec2:DescribeInstances",
      "iam:ListSSHPublicKeys",
      "iam:GetSSHPublicKey"
    ]

    resources = [
      "*"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "iam:GetGroup"
    ]

    resources = [
      "arn:aws:iam::*:group/ssrn.${var.environment}.ssh_users"
    ]
  }
}