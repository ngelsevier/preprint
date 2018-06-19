resource aws_iam_role_policy_attachment ecs_policy_on_ecs_service_scheduler {
  role = "${aws_iam_role.ecs_service_scheduler.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceRole"
}

resource aws_iam_role ecs_service_scheduler {
  name = "ssrn.${var.environment}.ecs_service_scheduler"
  assume_role_policy = "${data.terraform_remote_state.global.ecs_iam_trust_policy_json}"
}