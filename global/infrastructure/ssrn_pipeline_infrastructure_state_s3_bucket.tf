resource aws_s3_bucket ssrn_pipeline_infrastructure_state {
  count = "${length(var.ssrn_pipeline_environments)}"

  bucket = "elsevier-ssrn_pipeline-${var.ssrn_pipeline_environments[count.index]}"
  acl = "private"
  force_destroy = false

  versioning = {
    enabled = true
  }

  tags {
    contact = "${var.contact_details}"
    environment = "${var.ssrn_pipeline_environments[count.index]}"
    product = "ssrn pipeline"
  }

  lifecycle {
    prevent_destroy = true
  }
}