resource aws_ecs_task_definition api {
  family = "${var.product}-${var.environment}-${var.api_name}"
  container_definitions = "${data.template_file.api_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.search_api_iam_role_arn}"
}

data template_file api_container_definitions {
  template = "${file("${path.module}/files/api_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.api_name,"-","_")]}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ELASTICSEARCH_ENDPOINT", "value", format("https://%s", aws_elasticsearch_domain.papers_index.endpoint)))
    )))}"
    version = "__VERSION__"
  }
}