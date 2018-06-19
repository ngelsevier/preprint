resource aws_kinesis_stream papers {
  name = "${format("%s-papers", var.environment)}"
  shard_count = 1
  retention_period = 24

  tags {
    Name = "${var.product}-${var.environment}-${var.service_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}