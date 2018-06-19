resource aws_ecs_task_definition emitter {
  family = "${var.product}-${var.environment}-${var.emitter_name}"
  container_definitions = "${data.template_file.emitter_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.papers_emitter_iam_role_arn}"
}

data template_file emitter_container_definitions {
  template = "${file("emitter_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.emitter_name,"-","_")]}"
    service_name = "${var.emitter_name}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ENCRYPTED_DATABASE_PASSWORD", "value", var.encrypted_emitter_database_user_password)),
      jsonencode(map("name", "MAX_CONCURRENT_EMISSIONS", "value", var.emitter_max_concurrent_emissions)),
      jsonencode(map("name", "KINESIS_STREAM_NAME", "value", aws_kinesis_stream.papers.name))
    )))}"
    memoryReservation = "${var.emitter_memory_reservation}"
    version = "__VERSION__"
  }
}