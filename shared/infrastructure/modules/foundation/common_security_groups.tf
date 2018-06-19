resource aws_security_group outbound_http_to_anywhere {
  name = "${var.product}.${var.environment}.outbound_http_to_anywhere"
  vpc_id = "${aws_vpc.vpc.id}"

  egress {
    from_port = 80
    to_port = 80
    protocol = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  egress {
    from_port = 443
    to_port = 443
    protocol = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.outbound_http_to_anywhere"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group inbound_http_from_anywhere {
  name = "${var.product}.${var.environment}.inbound_http_from_anywhere"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.inbound_http_from_anywhere"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group inbound_http_from_elsevier {
  name = "${var.product}.${var.environment}.inbound_http_from_elsevier"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"

    cidr_blocks = [
      "${var.elsevier_cidrs}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.inbound_http_from_elsevier"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group inbound_https_from_elsevier {
  name = "${var.product}.${var.environment}.inbound_https_from_elsevier"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"

    cidr_blocks = [
      "${var.elsevier_cidrs}"
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.inbound_https_from_elsevier"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

resource aws_security_group ntp_access {
  name = "${var.product}.${var.environment}.ntp_access"
  vpc_id = "${aws_vpc.vpc.id}"

  ingress {
    from_port = 123
    to_port = 123
    protocol = "udp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  egress {
    from_port = 123
    to_port = 123
    protocol = "udp"

    cidr_blocks = [
      "0.0.0.0/0",
    ]
  }

  tags {
    Name = "${var.product}.${var.environment}.ntp_access"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}
