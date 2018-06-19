data terraform_remote_state authors_service {
  backend = "s3"

  config {
    bucket = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_names_by_environment[var.environment]}"
    key = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_keys["services_authors"]}"
    region = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_regions_by_environment[var.environment]}"
  }
}