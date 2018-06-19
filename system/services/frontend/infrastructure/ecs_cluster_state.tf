data terraform_remote_state ecs_cluster {
  backend = "s3"

  config {
    bucket = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_names_by_environment[var.environment]}"
    key = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_keys["container_scheduling_layer_services_cluster"]}"
    region = "${data.terraform_remote_state.global.ssrn_infrastructure_state_s3_bucket_regions_by_environment[var.environment]}"
  }
}