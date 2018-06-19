resource aws_iam_instance_profile bastion {
  name = "ssrn_pipeline.${var.environment}.bastion"
  role = "${aws_iam_role.iam_user_ssh_public_key_retriever.name}"
}