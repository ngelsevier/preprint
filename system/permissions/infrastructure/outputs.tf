output bastion_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.bastion.name}"
}

output ecs_container_instance_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.ecs_container_instance.name}"
}

output ecs_service_scheduler_iam_role_arn {
  value = "${aws_iam_role.ecs_service_scheduler.arn}"
}

output frontend_website_iam_role_arn {
  value = "${aws_iam_role.frontend_website.arn}"
}

output logging_logstash_iam_role_arn {
  value = "${aws_iam_role.logging_logstash.arn}"
}

output papers_emitter_iam_role_arn {
  value = "${aws_iam_role.papers_emitter.arn}"
}

output papers_replicator_iam_role_arn {
  value = "${aws_iam_role.papers_replicator.arn}"
}

output authors_updates_publisher_iam_role_arn {
  value = "${aws_iam_role.authors_updates_publisher.arn}"
}

output authors_replicator_iam_role_arn {
  value = "${aws_iam_role.authors_replicator.arn}"
}

output authors_database_heartbeat_lambda_executor_iam_role_arn {
  value = "${aws_iam_role.authors_database_heartbeat_lambda_executor.arn}"
}

output authors_replicator_scheduler_iam_role_arn {
  value = "${aws_iam_role.authors_replicator_scheduler.arn}"
}

output frontend_healthcheck_scheduler_iam_role_arn {
  value = "${aws_iam_role.frontend_healthcheck_scheduler.arn}"
}

output search_api_iam_role_arn {
  value = "${aws_iam_role.search_api.arn}"
}
output search_author_updates_subscriber_iam_role_arn {
  value = "${aws_iam_role.search_author_updates_subscriber.arn}"
}

output search_papers_consumer_iam_role_arn {
  value = "${aws_iam_role.search_papers_consumer.arn}"
}

output artifacts_publisher_iam_role_arn {
  value = "${aws_iam_role.artifacts_publisher.arn}"
}

output papers_database_heartbeat_lambda_executor_iam_role_arn {
  value = "${aws_iam_role.papers_database_heartbeat_lambda_executor.arn}"
}

output logging_janitor_lambda_executor_iam_role_arn {
  value = "${aws_iam_role.logging_janitor_lambda_executor.arn}"
}

output papers_replicator_scheduler_iam_role_arn {
  value = "${aws_iam_role.papers_replicator_scheduler.arn}"
}

output foundational_infrastructure_deployer_iam_role_arn {
  value = "${aws_iam_role.foundational_infrastructure_deployer.arn}"
}

output service_deployer_iam_role_arn {
  value = "${aws_iam_role.service_deployer.arn}"
}

output deployment_agent_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.deployment_agent.name}"
}

output database_administrator_iam_role_arn {
  value = "${aws_iam_role.database_administrator.arn}"
}

output database_administrator_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.database_administrator.name}"
}