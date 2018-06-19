resource aws_s3_bucket ssrn_infrastructure_state {
  count = "${length(var.ssrn_environments)}"

  bucket = "elsevier-ssrn-${var.ssrn_environments[count.index]}"
  acl = "private"
  force_destroy = false

  versioning = {
    enabled = true
  }

  tags {
    contact = "${var.contact_details}"
    environment = "${var.ssrn_environments[count.index]}"
    product = "ssrn"
  }

  lifecycle {
    prevent_destroy = true
  }
}