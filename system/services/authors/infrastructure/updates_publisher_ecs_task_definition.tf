resource aws_ecs_task_definition updates_publisher {
  family = "${var.product}-${var.environment}-${var.updates_publisher_name}"
  container_definitions = "${data.template_file.updates_publisher_container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.authors_updates_publisher_iam_role_arn}"
}

data template_file updates_publisher_container_definitions {
  template = "${file("files/updates_publisher_container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.updates_publisher_name,"-","_")]}"
    service_name = "${var.updates_publisher_name}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "ENCRYPTED_DATABASE_PASSWORD", "value", var.encrypted_publisher_database_user_password)),
      jsonencode(map("name", "MAX_CONCURRENT_EMISSIONS", "value", var.publisher_max_concurrent_emissions)),
      jsonencode(map("name", "KINESIS_STREAM_NAME", "value", aws_kinesis_stream.author_updates.name))
    )))}"
    memoryReservation = "${var.publisher_memory_reservation}"
    version = "__VERSION__"
  }
}