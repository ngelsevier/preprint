resource aws_subnet public {
  count                   = "${length(var.availability_zones)}"
  availability_zone       = "${var.availability_zones[count.index]}"
  cidr_block              = "${cidrsubnet(aws_vpc.vpc.cidr_block, length(var.availability_zones), count.index)}"
  map_public_ip_on_launch = false
  vpc_id                  = "${aws_vpc.vpc.id}"

  # Avoid recreating subnet if available availability zones change
  lifecycle {
    ignore_changes = [
      "availability_zone"
    ]
  }

  tags {
    Name        = "${var.product}-${var.environment}.public"
    contact     = "${var.contact_details}"
    environment = "${var.environment}"
    product     = "${var.product}"
  }
}

resource aws_route_table public {
  vpc_id = "${aws_vpc.vpc.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.internet_gateway.id}"
  }

  tags {
    Name        = "${var.product}-${var.environment}.public"
    contact     = "${var.contact_details}"
    environment = "${var.environment}"
    product     = "${var.product}"
  }
}

resource aws_route_table_association public {
  count          = "${length(var.availability_zones)}"
  route_table_id = "${aws_route_table.public.id}"
  subnet_id      = "${element(aws_subnet.public.*.id, count.index)}"
}