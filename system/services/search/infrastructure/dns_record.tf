resource aws_route53_record service {
  zone_id = "${data.terraform_remote_state.vpc.internal_route53_hosted_zone_id}"
  name = "${var.service_name}"
  type = "A"

  alias {
    name = "${aws_alb.service.dns_name}"
    zone_id = "${aws_alb.service.zone_id}"
    evaluate_target_health = true
  }
}