resource aws_iam_role authors_replicator {
  name = "ssrn.${var.environment}.authors_replicator"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}

resource aws_iam_role_policy kms_decryption_on_authors_replicator_role {
  name = "ssrn.${var.environment}.kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.authors_replicator.name}"
}