resource aws_ecs_task_definition logstash {
  family = "${var.product}-${var.environment}-${var.logstash_name}"
  container_definitions = "${data.template_file.logstash_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.logging_logstash_iam_role_arn}"
}

data template_file logstash_container_definitions {
  template = "${file("${path.module}/files/logstash_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.logstash_name,"-","_")]}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "AWS_ELASTICSEARCH_HOST", "value", aws_elasticsearch_domain.logging_index.endpoint)),
      jsonencode(map("name", "AWS_ELASTICSEARCH_REGION", "value", var.aws_region)),
      jsonencode(map("name", "AWS_ELASTICSEARCH_PORT", "value", var.aws_elasticsearch_port)),
      jsonencode(map("name", "AWS_ELASTICSEARCH_PROTOCOL", "value", var.aws_elasticsearch_protocol))
    )))}"
    logstash_port = "${var.logstash_port}"
    version = "__VERSION__"
  }
}