resource aws_route53_zone ssrn2_com {
  name = "ssrn2.com"

  tags {
    contact = "${var.contact_details}"
    product = "ssrn"
  }
}