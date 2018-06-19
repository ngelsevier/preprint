resource aws_route53_record service {
  zone_id = "${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
  name = "${format("www%s", var.environment == "production" ? "": format("-%s", var.environment))}"
  type = "A"

  alias {
    name = "${aws_alb.service.dns_name}"
    zone_id = "${aws_alb.service.zone_id}"
    evaluate_target_health = true
  }
}