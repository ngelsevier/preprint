data aws_iam_policy_document pipeline_artifact_publication {
  statement = {
    effect = "Allow"

    actions = [
      "s3:GetObject",
      "s3:PutObject"
    ]

    resources = [
      "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_arns_by_environment[var.environment]}/artifacts/*"
    ]
  }
}