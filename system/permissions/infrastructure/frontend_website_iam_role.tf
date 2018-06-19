resource aws_iam_role frontend_website {
  name = "ssrn.${var.environment}.frontend_website"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}

resource aws_iam_role_policy kms_decryption_on_frontend_website_role {
  name = "ssrn.${var.environment}.kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.frontend_website.name}"
}