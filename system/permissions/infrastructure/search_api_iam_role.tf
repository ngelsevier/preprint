resource aws_iam_role search_api {
  name = "ssrn.${var.environment}.search_api"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}
