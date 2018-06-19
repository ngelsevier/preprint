output ssrn2_com_route_53_hosted_zone_id {
  value = "${aws_route53_zone.ssrn2_com.id}"
}

output wildcard_ssrn2_com_acm_certificate_arn {
  value = "${data.aws_acm_certificate.wildcard_ssrn2_com.arn}"
}

output ec2_iam_trust_policy_json {
  value = "${data.aws_iam_policy_document.ec2_trust_policy.json}"
}

output ecs_iam_trust_policy_json {
  value = "${data.aws_iam_policy_document.ecs_trust_policy.json}"
}

output ecs_tasks_trust_policy_json {
  value = "${data.aws_iam_policy_document.ecs_tasks_trust_policy.json}"
}

output current_aws_account_trust_policy_json {
  value = "${data.aws_iam_policy_document.current_aws_account_trust_policy.json}"
}

output global_infrastructure_state_s3_bucket_domain_name {
  value = "${aws_s3_bucket.ssrn_global_infrastructure_state.bucket_domain_name}"
}

output global_infrastructure_state_s3_bucket_arn {
  value = "${aws_s3_bucket.ssrn_global_infrastructure_state.arn}"
}

output global_infrastructure_state_s3_key_arn {
  value = "${format("%v/%v", aws_s3_bucket.ssrn_global_infrastructure_state.arn, "global.tfstate")}"
}

output lambda_trust_policy_json {
  value = "${data.aws_iam_policy_document.lambda_trust_policy.json}"
}

output ssrn_infrastructure_state_s3_bucket_arns_by_environment {
  value = "${zipmap(var.ssrn_environments, aws_s3_bucket.ssrn_infrastructure_state.*.arn)}"
}

output ssrn_infrastructure_state_s3_bucket_names_by_environment {
  value = "${zipmap(var.ssrn_environments, aws_s3_bucket.ssrn_infrastructure_state.*.id)}"
}

output ssrn_infrastructure_state_s3_bucket_regions_by_environment {
  value = "${zipmap(var.ssrn_environments, aws_s3_bucket.ssrn_infrastructure_state.*.region)}"
}

output ssrn_infrastructure_state_s3_keys {
  value = "${var.ssrn_infrastructure_state_s3_keys}"
}

output ssrn_permissions_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["permissions"]))}"
}

output ssrn_vpc_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["vpc"]))}"
}

output ssrn_container_scheduling_layer_services_cluster_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["container_scheduling_layer_services_cluster"]))}"
}

output ssrn_services_authors_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["services_authors"]))}"
}

output ssrn_services_frontend_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["services_frontend"]))}"
}

output ssrn_services_search_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["services_search"]))}"
}

output ssrn_services_papers_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["services_papers"]))}"
}

output ssrn_services_logging_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_infrastructure_state.*.arn, var.ssrn_infrastructure_state_s3_keys["services_logging"]))}"
}

output ssrn_docker_image_repository_urls_by_service_name {
  value = {
    frontend_website = "${module.ssrn_frontend_website_ecr_repository.repository_url}"
    papers_emitter = "${module.ssrn_papers_emitter_ecr_repository.repository_url}"
    logging_logstash = "${module.ssrn_logging_logstash_ecr_repository.repository_url}"
    papers_replicator = "${module.ssrn_papers_replicator_ecr_repository.repository_url}"
    authors_replicator = "${module.ssrn_authors_replicator_ecr_repository.repository_url}"
    authors_updates_publisher = "${module.ssrn_authors_updates_publisher_ecr_repository.repository_url}"
    search_api = "${module.ssrn_search_api_ecr_repository.repository_url}"
    search_papers_consumer = "${module.ssrn_search_papers_consumer_ecr_repository.repository_url}"
    search_author_updates_subscriber = "${module.ssrn_search_author_updates_subscriber_ecr_repository.repository_url}"
  }
}

output ssrn_pipeline_infrastructure_state_s3_keys {
  value = "${var.ssrn_pipeline_infrastructure_state_s3_keys}"
}

output ssrn_pipeline_infrastructure_state_s3_bucket_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments, aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn)}"
}

output ssrn_pipeline_infrastructure_state_s3_bucket_names_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments, aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.id)}"
}

output ssrn_pipeline_infrastructure_state_s3_bucket_regions_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments, aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.region)}"
}

output ssrn_pipeline_controller_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["controller"]))}"
}

output ssrn_pipeline_permissions_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["permissions"]))}"
}

output ssrn_pipeline_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["pipeline"]))}"
}

output ssrn_pipeline_container_scheduling_layer_pipeline_cluster_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["container_scheduling_layer_pipeline_cluster"]))}"
}

output ssrn_pipeline_services_reverse_proxy_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["services_reverse_proxy"]))}"
}

output ssrn_pipeline_services_go_server_infrastructure_state_s3_key_arns_by_environment {
  value = "${zipmap(var.ssrn_pipeline_environments,  formatlist("%v/%v", aws_s3_bucket.ssrn_pipeline_infrastructure_state.*.arn, var.ssrn_pipeline_infrastructure_state_s3_keys["services_go_server"]))}"
}

output ssrn_ecr_image_builder_iam_role_assumption_policy_json {
  value = "${data.aws_iam_policy_document.ssrn_ecr_image_builder_iam_role_assumption.json}"
}

output ssrn_ecr_image_consumer_iam_role_assumption_policy_json {
  value = "${data.aws_iam_policy_document.ssrn_ecr_image_consumer_iam_role_assumption.json}"
}