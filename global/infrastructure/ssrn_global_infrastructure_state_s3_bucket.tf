resource aws_s3_bucket ssrn_global_infrastructure_state {
  bucket = "elsevier-ssrn"
  acl = "private"
  force_destroy = false

  versioning = {
    enabled = true
  }

  tags {
    contact = "${var.contact_details}"
    product = "ssrn"
  }

  lifecycle {
    prevent_destroy = true
  }
}