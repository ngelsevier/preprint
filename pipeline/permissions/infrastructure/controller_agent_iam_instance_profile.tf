resource aws_iam_instance_profile controller_agent {
  name = "ssrn_pipeline.${var.environment}.controller-agent"
  role = "${aws_iam_role.controller_agent.name}"
}

resource aws_iam_role controller_agent {
  name = "ssrn_pipeline.${var.environment}.controller_agent"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_controller_agent {
  role = "${aws_iam_role.controller_agent.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_role_policy pipeline_deployer_iam_role_assumption {
  name = "ssrn_pipeline.${var.environment}.controller_deployer_iam_role_assumption"
  policy = "${data.aws_iam_policy_document.pipeline_deployer_iam_role_assumption.json}"
  role = "${aws_iam_role.controller_agent.id}"
}

data aws_iam_policy_document pipeline_deployer_iam_role_assumption {
  statement = {
    effect = "Allow"

    actions = [
      "sts:AssumeRole"
    ]

    resources = [
      "${aws_iam_role.pipeline_deployer.arn}"
    ]
  }
}

resource aws_iam_role_policy controller_automation_agent_s3_artifact_retrieval {
  name = "ssrn_pipeline.${var.environment}.controller_automation_agent_s3_artifact_retrieval"
  policy = "${data.aws_iam_policy_document.controller_automation_agent_s3_artifact_retrieval.json}"
  role = "${aws_iam_role.controller_agent.id}"
}

data aws_iam_policy_document controller_automation_agent_s3_artifact_retrieval {
  statement = {
    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      "arn:aws:s3:::elsevier-ssrn_pipeline-artifacts-${var.environment}"
    ]
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject",
      "s3:ListObjects"
    ]

    resources = [
      "arn:aws:s3:::elsevier-ssrn_pipeline-artifacts-${var.environment}/*"
    ]
  }
}
