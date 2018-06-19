resource aws_iam_instance_profile production_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.production-deployment-agent"
  role = "${aws_iam_role.production_deployment_agent.name}"
}

resource aws_iam_role production_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.production_deployment_agent"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_production_deployment_agent {
  role = "${aws_iam_role.production_deployment_agent.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role_policy production_system_deployer_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.production_system_deployer_iam_roles_assumption"
  policy = "${data.aws_iam_policy_document.production_system_deployer_iam_role_assumption.json}"
  role = "${aws_iam_role.production_deployment_agent.id}"
}

data aws_iam_policy_document production_system_deployer_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${data.terraform_remote_state.production_system_permissions.foundational_infrastructure_deployer_iam_role_arn}"
    ]
  }
}

resource aws_iam_role_policy controller_deployer_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.controller_deployer_iam_role_assumption"
  policy = "${data.aws_iam_policy_document.controller_deployer_iam_role_assumption.json}"
  role = "${aws_iam_role.production_deployment_agent.id}"
}

data aws_iam_policy_document controller_deployer_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${aws_iam_role.controller_deployer.arn}"
    ]
  }
}

resource aws_iam_role_policy ssrn_ecr_image_builder_iam_role_assumption_on_production_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.ssrn_ecr_image_builder_iam_role_assumption_on_production_deployment_agent"
  policy = "${data.terraform_remote_state.global.ssrn_ecr_image_builder_iam_role_assumption_policy_json}"
  role = "${aws_iam_role.production_deployment_agent.name}"
}

resource aws_iam_role_policy pipeline_artifact_publication_on_deployment_agent {
  name = "ssrn_pipeline.${var.environment}.pipeline_artifact_publication_on_deployment_agent"
  policy = "${data.aws_iam_policy_document.pipeline_artifact_publication.json}"
  role = "${aws_iam_role.production_deployment_agent.id}"
}