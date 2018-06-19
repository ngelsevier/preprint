output iam_role_arn {
  value = "${aws_iam_role.ecr_image_builder.arn}"
}