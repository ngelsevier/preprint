resource aws_iam_instance_profile ci_agent {
  name = "ssrn_pipeline.${var.environment}.ci-agent"
  role = "${aws_iam_role.ci_agent.name}"
}

resource aws_iam_role ci_agent {
  name = "ssrn_pipeline.${var.environment}.ci_agent"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_ci_agent {
  role = "${aws_iam_role.ci_agent.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role_policy pipeline_artifact_publication_on_ci_agent {
  name = "ssrn_pipeline.${var.environment}.pipeline_artifact_publication_on_ci_agent"
  policy = "${data.aws_iam_policy_document.pipeline_artifact_publication.json}"
  role = "${aws_iam_role.ci_agent.id}"
}

resource aws_iam_role_policy production_system_artifacts_publisher_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.production_system_artifacts_publisher_iam_role_assumption"
  policy = "${data.aws_iam_policy_document.production_system_artifacts_publisher_iam_role_assumption.json}"
  role = "${aws_iam_role.ci_agent.id}"
}

data aws_iam_policy_document production_system_artifacts_publisher_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${data.terraform_remote_state.production_system_permissions.artifacts_publisher_iam_role_arn}"
    ]
  }
}

resource aws_iam_role_policy ansible_role_publisher_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.ansible_role_publisher_iam_role_assumption"
  policy = "${data.aws_iam_policy_document.ansible_role_publisher_iam_role_assumption.json}"
  role = "${aws_iam_role.ci_agent.id}"
}

data aws_iam_policy_document ansible_role_publisher_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${aws_iam_role.ansible_role_publisher.arn}"
    ]
  }
}

resource aws_iam_role_policy ssrn_ecr_image_builder_iam_role_assumption_on_ci_agent_role {
  name = "ssrn_pipeline.${var.environment}.ssrn_ecr_image_builder_iam_role_assumption_on_ci_agent_role"
  policy = "${data.terraform_remote_state.global.ssrn_ecr_image_builder_iam_role_assumption_policy_json}"
  role = "${aws_iam_role.ci_agent.name}"
}

resource aws_iam_role_policy ssrn_ecr_image_consumer_iam_role_assumption_on_ci_agent_role {
  name = "ssrn_pipeline.${var.environment}.ssrn_ecr_image_consumer_iam_role_assumption_on_ci_agent_role"
  policy = "${data.terraform_remote_state.global.ssrn_ecr_image_consumer_iam_role_assumption_policy_json}"
  role = "${aws_iam_role.ci_agent.name}"
}

resource aws_iam_role_policy ci_agent_s3_artifact_publication {
  name = "ssrn_pipeline.${var.environment}.ci_agent_s3_artifact_publication"
  policy = "${data.aws_iam_policy_document.ci_agent_s3_artifact_publication.json}"
  role = "${aws_iam_role.ci_agent.id}"
}

data aws_iam_policy_document ci_agent_s3_artifact_publication {
  statement = {
    effect = "Allow"

    actions = [
      "s3:PutObject"
    ]

    resources = [
      "arn:aws:s3:::elsevier-ssrn_pipeline-artifacts-${var.environment}/*"
    ]
  }
}
