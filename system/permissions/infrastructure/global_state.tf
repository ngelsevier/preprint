data terraform_remote_state global {
  backend = "s3"

  config {
    bucket = "${var.global_remote_state_s3_bucket}"
    key = "${var.global_remote_state_s3_key}"
    region = "${var.global_remote_state_s3_region}"
  }
}