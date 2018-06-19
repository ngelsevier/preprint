resource aws_s3_bucket artifacts {

  bucket = "elsevier-${var.product}-artifacts-${var.environment}"
  acl = "private"
  force_destroy = false

  tags {
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }

  lifecycle {
    prevent_destroy = true
  }
}