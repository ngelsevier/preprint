resource aws_iam_instance_profile controller_automation_server {
  name = "ssrn_pipeline.${var.environment}.controller-automation-server"
  role = "${aws_iam_role.controller_automation_server.name}"
}

resource aws_iam_role controller_automation_server {
  name = "ssrn_pipeline.${var.environment}.controller_automation_server"
  assume_role_policy = "${data.terraform_remote_state.global.ec2_iam_trust_policy_json}"
}

resource aws_iam_role_policy_attachment controller_automation_server_backup_on_automation_server {
  role = "${aws_iam_role.controller_automation_server.name}"
  policy_arn = "${aws_iam_policy.controller_automation_server_backup.arn}"
}

resource aws_iam_role_policy_attachment iam_user_ssh_public_key_retrieval_on_controller_automation_server {
  role = "${aws_iam_role.controller_automation_server.name}"
  policy_arn = "${aws_iam_policy.iam_user_ssh_public_key_retrieval.arn}"
}

resource aws_iam_policy controller_automation_server_backup {
  name = "ssrn_pipeline.${var.environment}.controller_automation_server_backup"
  path = "/"
  policy = "${data.aws_iam_policy_document.controller_automation_server_backup.json}"
}

data aws_iam_policy_document controller_automation_server_backup {
  statement = {
    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}"
    ]

    condition {
      test = "StringLike"
      variable = "s3:prefix"

      values = [
        "controller-automation-server/*"
      ]
    }
  }

  statement = {
    effect = "Allow"

    actions = [
      "s3:DeleteObject",
      "s3:DeleteObjectVersion",
      "s3:GetObject",
      "s3:PutObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}/controller-automation-server/*"
    ]

  }
}

resource aws_iam_role_policy controller_automation_server_kms_decryption {
  name = "ssrn_pipeline.${var.environment}.controller_automation_server_kms_decryption"
  policy = "${data.aws_iam_policy_document.kms_decryption.json}"
  role = "${aws_iam_role.controller_automation_server.name}"
}

resource aws_iam_role_policy controller_automation_server_s3_artifact_retrieval {
  name = "ssrn_pipeline.${var.environment}.controller_automation_server_s3_artifact_retrieval"
  policy = "${data.aws_iam_policy_document.controller_automation_server_s3_artifact_retrieval.json}"
  role = "${aws_iam_role.controller_automation_server.id}"
}

data aws_iam_policy_document controller_automation_server_s3_artifact_retrieval {
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