resource aws_ecs_task_definition author_updates_subscriber_service {
  family = "${var.product}-${var.environment}-${var.author_updates_subscriber_name}"
  container_definitions = "${data.template_file.author_updates_subscriber_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.search_author_updates_subscriber_iam_role_arn}"
}

data template_file author_updates_subscriber_container_definitions {
  template = "${file("${path.module}/files/author_updates_subscriber_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.author_updates_subscriber_name,"-","_")]}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ELASTICSEARCH_ENDPOINT", "value", format("https://%s", aws_elasticsearch_domain.papers_index.endpoint))),
      jsonencode(map("name", "ELASTICSEARCH_SCROLL_SIZE", "value", var.elasticsearch_scroll_size)),
      jsonencode(map("name", "KINESIS_STREAM_NAME", "value", data.terraform_remote_state.authors_service.kinesis_stream_name)),
      jsonencode(map("name", "KCL_APPLICATION_NAME", "value", format("%s-authorUpdatesSubscriber", var.environment)))
    )))}"
    version = "__VERSION__"
  }
}