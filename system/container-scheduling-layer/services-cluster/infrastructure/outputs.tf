output cluster_name {
  value = "${aws_ecs_cluster.cluster.name}"
}

output cluster_arn {
  value = "${aws_ecs_cluster.cluster.id}"
}

output ecs_service_load_balancer_security_group_id {
  value = "${aws_security_group.ecs_service_load_balancer.id}"
}

output internal_service_load_balancer_security_group_id {
  value = "${aws_security_group.internal_service_load_balancer.id}"
}

output internal_service_client_security_group_id {
  value = "${aws_security_group.internal_service_client.id}"
}