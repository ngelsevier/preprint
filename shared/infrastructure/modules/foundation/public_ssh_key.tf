resource aws_key_pair default {
  key_name_prefix = "${var.product}.${var.environment}.default."
  public_key = "${var.public_ssh_key}"
}
