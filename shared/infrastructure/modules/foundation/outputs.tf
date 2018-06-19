output bastion_accessible_instance_security_group_id {
  value = "${aws_security_group.bastion_accessible_instance.id}"
}

output default_ssh_key_name {
  value = "${aws_key_pair.default.key_name}"
}

output inbound_http_from_anywhere_security_group_id {
  value = "${aws_security_group.inbound_http_from_anywhere.id}"
}

output inbound_http_from_elsevier_security_group_id {
  value = "${aws_security_group.inbound_http_from_elsevier.id}"
}

output inbound_https_from_elsevier_security_group_id {
  value = "${aws_security_group.inbound_https_from_elsevier.id}"
}

output outbound_http_to_anywhere_security_group_id {
  value = "${aws_security_group.outbound_http_to_anywhere.id}"
}

output ntp_access_security_group_id {
  value = "${aws_security_group.ntp_access.id}"
}

output private_subnet_ids {
  value = [
    "${aws_subnet.private.*.id}"
  ]
}

output public_subnet_ids {
  value = [
    "${aws_subnet.public.*.id}"
  ]
}

output vpc_cidr_block {
  value = "${aws_vpc.vpc.cidr_block}"
}

output vpc_id {
  value = "${aws_vpc.vpc.id}"
}

output internal_route53_hosted_zone_id {
  value = "${aws_route53_zone.internal.id}"
}

output default_ami_id {
  value = "${data.aws_ami.default.id}"
}