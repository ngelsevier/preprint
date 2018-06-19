data terraform_remote_state papers_service {
  backend = "s3"

  config {
    bucket = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_names_by_environment[var.environment]}"
    key = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_keys["services_papers"]}"
    region = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_regions_by_environment[var.environment]}"
  }
}