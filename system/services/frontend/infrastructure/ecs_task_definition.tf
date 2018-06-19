resource aws_ecs_task_definition website {
  family = "${var.product}-${var.environment}-${var.website_name}"
  container_definitions = "${data.template_file.container_definitions.rendered}"
  network_mode = "bridge"
  task_role_arn = "${data.terraform_remote_state.permissions.frontend_website_iam_role_arn}"
}

data template_file container_definitions {
  template = "${file("${path.module}/files/container_definitions.json.tpl")}"

  vars {
    docker_image_repository_url = "${data.terraform_remote_state.global.ssrn_docker_image_repository_urls_by_service_name[replace(var.website_name,"-","_")]}"
    environment_variables_json_array = "${format("[ %s ]", join(", ", list(
      jsonencode(map("name", "OLD_PLATFORM_ARTICLE_PAGE_BASE_URL", "value", var.old_platform_article_page_base_url)),
      jsonencode(map("name", "OLD_PLATFORM_AUTHOR_PROFILE_PAGE_BASE_URL", "value", var.old_platform_author_profile_page_base_url)),
      jsonencode(map("name", "OLD_PLATFORM_AUTHOR_IMAGE_BASE_URL", "value", var.old_platform_author_image_base_url)),
      jsonencode(map("name", "OLD_PLATFORM_AUTH_BASE_URL", "value", var.old_platform_auth_base_url)),
      jsonencode(map("name", "ENCRYPTED_FRONTEND_WEBSITE_USERNAME", "value", var.encrypted_frontend_website_username)),
      jsonencode(map("name", "ENCRYPTED_FRONTEND_WEBSITE_PASSWORD", "value", var.encrypted_frontend_website_password)),
      jsonencode(map("name", "SEARCH_RESULT_PAGE_SIZE", "value", var.search_result_page_size))
    )))}"

    version = "__VERSION__"
  }
}