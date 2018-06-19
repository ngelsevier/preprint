resource aws_nat_gateway nat_gateway {
  count         = "${length(var.availability_zones)}"
  allocation_id = "${element(aws_eip.nat_gateway.*.id, count.index)}"
  subnet_id     = "${element(aws_subnet.public.*.id, count.index)}"

  depends_on = [
    "aws_internet_gateway.internet_gateway",
  ]
}

resource aws_eip nat_gateway {
  count = "${length(var.availability_zones)}"
  vpc   = true
}
