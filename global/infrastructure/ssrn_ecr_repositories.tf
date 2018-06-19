module ssrn_base_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "base"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_java_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "java"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_java_aws_es_proxy_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "java-aws-es-proxy"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_nginx_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "nginx"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_frontend_website_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "frontend-website"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_papers_emitter_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "papers-emitter"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_logging_logstash_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "logging-logstash"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_papers_replicator_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "papers-replicator"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_authors_replicator_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "authors-replicator"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_authors_updates_publisher_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "authors-updates-publisher"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_search_api_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "search-api"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_search_papers_consumer_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "search-papers-consumer"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}

module ssrn_search_author_updates_subscriber_ecr_repository {
  source = "./modules/aws_ecr_repository"
  product = "ssrn"
  image_name = "search-author-updates-subscriber"
  ecr_push_and_pull_role_arn = "${module.ssrn_ecr_image_builder_iam_role.iam_role_arn}"
}