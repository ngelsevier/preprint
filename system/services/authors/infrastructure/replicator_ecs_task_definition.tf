resource aws_ecs_task_definition authors_replicator {
  family = "${var.product}-${var.environment}-${var.replicator_name}"
  container_definitions = "${data.template_file.replicator_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.authors_replicator_iam_role_arn}"
}

data template_file replicator_container_definitions {
  template = "${file("files/replicator_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.replicator_name,"-","_")]}"
    service_name = "${var.replicator_name}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ENCRYPTED_DATABASE_PASSWORD", "value", var.encrypted_replicator_database_user_password)),
      jsonencode(map("name", "EVENTS_FEED_BASE_URL", "value", var.old_platform_events_feed_base_url)),
      jsonencode(map("name", "EVENTS_FEED_HTTP_BASIC_AUTH_USERNAME", "value", var.old_platform_events_feed_http_basic_auth_username)),
      jsonencode(map("name", "EVENTS_FEED_HTTP_BASIC_AUTH_PASSWORD", "value", var.old_platform_events_feed_http_basic_auth_password)),
      jsonencode(map("name", "ENTITY_FEED_MAX_PAGE_REQUEST_RETRIES", "value", var.entity_feed_max_page_request_retries))
    )))}"
    memoryReservation = "${var.replicator_memory_reservation}"
    version = "__VERSION__"
  }
}