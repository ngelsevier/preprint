resource aws_subnet private {
  count = "${length(var.availability_zones)}"
  availability_zone = "${var.availability_zones[count.index]}"
  cidr_block = "${cidrsubnet(aws_vpc.vpc.cidr_block, length(var.availability_zones), length(var.availability_zones) + count.index)}"
  map_public_ip_on_launch = false
  vpc_id = "${aws_vpc.vpc.id}"

  # Avoid recreating subnet if available availability zones change
  lifecycle {
    ignore_changes = [
      "availability_zone"
    ]
  }

  tags {
    Name = "${var.product}-${var.environment}.private"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_route_table private {
  count = "${length(var.availability_zones)}"
  vpc_id = "${aws_vpc.vpc.id}"

  route {
    cidr_block = "0.0.0.0/0"
    nat_gateway_id = "${element(aws_nat_gateway.nat_gateway.*.id, count.index)}"
  }

  tags {
    Name = "${var.product}-${var.environment}.private.${element(aws_subnet.private.*.availability_zone, count.index)}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_route_table_association private {
  count = "${length(var.availability_zones)}"
  route_table_id = "${element(aws_route_table.private.*.id, count.index)}"
  subnet_id = "${element(aws_subnet.private.*.id, count.index)}"
}
