data terraform_remote_state permissions {
  backend = "s3"

  config {
    bucket = "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_names_by_environment[var.environment]}"
    key = "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_keys["permissions"]}"
    region = "${data.terraform_remote_state.global.ssrn_pipeline_infrastructure_state_s3_bucket_regions_by_environment[var.environment]}"
  }
}