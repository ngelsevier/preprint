resource aws_iam_instance_profile qa_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.qa-deployment-agent"
  role = "${aws_iam_role.qa_deployment_agent.name}"
}

resource aws_iam_role qa_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.qa_deployment_agent"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_qa_deployment_agent {
  role = "${aws_iam_role.qa_deployment_agent.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role_policy qa_system_deployer_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.qa_system_deployer_iam_roles_assumption"
  policy = "${data.aws_iam_policy_document.qa_system_deployer_iam_role_assumption.json}"
  role = "${aws_iam_role.qa_deployment_agent.id}"
}

data aws_iam_policy_document qa_system_deployer_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${data.terraform_remote_state.qa_system_permissions.foundational_infrastructure_deployer_iam_role_arn}"
    ]
  }
}

resource aws_iam_role_policy ssrn_ecr_image_builder_iam_role_assumption_on_qa_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.ssrn_ecr_image_builder_iam_role_assumption_on_qa_deployment_agent"
  policy = "${data.terraform_remote_state.global.ssrn_ecr_image_builder_iam_role_assumption_policy_json}"
  role = "${aws_iam_role.qa_deployment_agent.name}"
}
