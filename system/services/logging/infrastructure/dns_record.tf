resource aws_route53_record service {
  zone_id = "${data.terraform_remote_state.vpc.internal_route53_hosted_zone_id}"
  name = "${var.logstash_dns_name}"
  type = "A"

  alias {
    name = "${aws_elb.logstash.dns_name}"
    zone_id = "${aws_elb.logstash.zone_id}"
    evaluate_target_health = true
  }
}