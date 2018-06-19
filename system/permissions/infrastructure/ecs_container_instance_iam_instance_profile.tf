resource "aws_iam_instance_profile" "ecs_container_instance" {
  name = "ssrn.${var.environment}.ecs_container_instance"
  role = "${aws_iam_role.ecs_container_instance.name}"
}

resource aws_iam_role_policy_attachment ecs_policy_on_ecs_container_instance_role {
  role = "${aws_iam_role.ecs_container_instance.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_policy_on_ecs_container_instance_role {
  role = "${aws_iam_role.ecs_container_instance.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role ecs_container_instance {
  name = "ssrn.${var.environment}.ecs_container_instance"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}