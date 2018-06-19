resource aws_iam_instance_profile deployment_agent {
  name = "ssrn.${var.environment}.deployment-agent"
  role = "${aws_iam_role.deployment_agent.name}"
}

resource aws_iam_role deployment_agent {
  name = "ssrn.${var.environment}.deployment_agent"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_deployment_agent {
  role = "${aws_iam_role.deployment_agent.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role_policy service_deployer_iam_roles_assumption {
  name = "ssrn_pipeline.${var.environment}.service_deployer_iam_roles_assumption"
  policy = "${data.aws_iam_policy_document.service_deployer_iam_roles_assumption.json}"
  role = "${aws_iam_role.deployment_agent.id}"
}

data aws_iam_policy_document service_deployer_iam_roles_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${aws_iam_role.service_deployer.arn}"
    ]
  }
}

resource aws_iam_role_policy container_scheduling_layer_deployer_iam_roles_assumption {
  name = "ssrn_pipeline.${var.environment}.container_scheduling_layer_deployer_iam_roles_assumption"
  policy = "${data.aws_iam_policy_document.container_scheduling_layer_deployer_iam_roles_assumption.json}"
  role = "${aws_iam_role.deployment_agent.id}"
}

data aws_iam_policy_document container_scheduling_layer_deployer_iam_roles_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${aws_iam_role.container_scheduling_layer_deployer.arn}"
    ]
  }
}