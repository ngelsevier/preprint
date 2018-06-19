resource aws_security_group postgres_database {
  name = "ssrn.${var.environment}.postgres"
  vpc_id = "${module.foundation.vpc_id}"

  tags {
    Name = "ssrn.${var.environment}.postgres"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "ssrn"
  }
}

resource aws_security_group postgres_client {
  name = "ssrn.${var.environment}.postgres_client"
  vpc_id = "${module.foundation.vpc_id}"

  egress {
    from_port = "${var.postgres_database_port}"
    to_port = "${var.postgres_database_port}"
    protocol = "tcp"

    security_groups = [
      "${aws_security_group.postgres_database.id}"
    ]
  }

  tags {
    Name = "ssrn.${var.environment}.postgres_client"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "ssrn"
  }
}

resource aws_security_group_rule postgres_client_to_postgres_database {
  from_port = "${var.postgres_database_port}"
  to_port = "${var.postgres_database_port}"
  protocol = "tcp"
  security_group_id = "${aws_security_group.postgres_database.id}"
  type = "ingress"
  source_security_group_id = "${aws_security_group.postgres_client.id}"
}

resource aws_security_group_rule deployment_agent_to_postgres_database {
  from_port = "${var.postgres_database_port}"
  to_port = "${var.postgres_database_port}"
  protocol = "tcp"
  security_group_id = "${aws_security_group.postgres_database.id}"
  type = "ingress"
  source_security_group_id = "${aws_security_group.deployment_agent.id}"
}