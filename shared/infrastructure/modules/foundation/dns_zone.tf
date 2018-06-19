resource aws_route53_zone internal {
  name = "internal-service."
  vpc_id = "${aws_vpc.vpc.id}"

  tags {
    Name = "${var.product}-${var.environment}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}
