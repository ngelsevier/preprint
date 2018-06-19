resource aws_iam_role logging_logstash {
  name = "ssrn.${var.environment}.logging_logstash"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_tasks_trust_policy_json}"
}