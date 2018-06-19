resource aws_ecs_task_definition papers_consumer {
  family = "${var.product}-${var.environment}-${var.papers_consumer_name}"
  container_definitions = "${data.template_file.papers_consumer_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.search_papers_consumer_iam_role_arn}"
}

data template_file papers_consumer_container_definitions {
  template = "${file("${path.module}/files/papers_consumer_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.papers_consumer_name,"-","_")]}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ELASTICSEARCH_ENDPOINT", "value", format("https://%s", aws_elasticsearch_domain.papers_index.endpoint))),
      jsonencode(map("name", "KINESIS_STREAM_NAME", "value", data.terraform_remote_state.papers_service.kinesis_stream_name)),
      jsonencode(map("name", "KCL_APPLICATION_NAME", "value", format("%s-paperConsumer", var.environment)))
    )))}"
    version = "__VERSION__"
  }
}