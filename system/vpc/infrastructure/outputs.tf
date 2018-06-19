output bastion_accessible_instance_security_group_id {
  value = "${module.foundation.bastion_accessible_instance_security_group_id}"
}

output default_ssh_key_name {
  value = "${module.foundation.default_ssh_key_name}"
}

output inbound_http_from_anywhere_security_group_id {
  value = "${module.foundation.inbound_http_from_anywhere_security_group_id}"
}

output internal_route53_hosted_zone_id {
  value = "${module.foundation.internal_route53_hosted_zone_id}"
}

output outbound_http_to_anywhere_security_group_id {
  value = "${module.foundation.outbound_http_to_anywhere_security_group_id}"
}

output ntp_access_security_group_id {
  value = "${module.foundation.ntp_access_security_group_id}"
}

output postgres_client_security_group_id {
  value = "${aws_security_group.postgres_client.id}"
}

output postgres_database_port {
  value = "${var.postgres_database_port}"
}

output postgres_database_security_group_id {
  value = "${aws_security_group.postgres_database.id}"
}

output private_subnet_ids {
  value = "${module.foundation.private_subnet_ids}"
}

output public_subnet_ids {
  value = "${module.foundation.public_subnet_ids}"
}

output sns_cloudwatch_alarm_topic {
  value = "${aws_sns_topic.sns_cloudwatch_alarm_topic.id}"
}

output vpc_cidr_block {
  value = "${module.foundation.vpc_cidr_block}"
}

output vpc_id {
  value = "${module.foundation.vpc_id}"
}