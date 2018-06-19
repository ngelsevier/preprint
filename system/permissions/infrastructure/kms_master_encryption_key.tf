resource aws_kms_key master_encryption_key {
  tags {
    contact     = "${var.contact_details}"
    environment = "${var.environment}"
    product     = "ssrn"
  }
}

resource aws_kms_alias master_encryption_key {
  name = "alias/ssrn-${var.environment}"
  target_key_id = "${aws_kms_key.master_encryption_key.key_id}"
}

data aws_iam_policy_document kms_decryption {

  statement = {
    effect = "Allow"

    actions = [
      "kms:Decrypt"
    ]

    resources = [
      "${aws_kms_key.master_encryption_key.arn}"
    ]
  }
}