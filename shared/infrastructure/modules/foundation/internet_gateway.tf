resource aws_internet_gateway internet_gateway {
  vpc_id = "${aws_vpc.vpc.id}"

  tags {
    Name        = "${var.product}-${var.environment}"
    contact     = "${var.contact_details}"
    environment = "${var.environment}"
    product     = "${var.product}"
  }
}
