output content_blobs {
  value = [
    "${data.template_file.user_data.*.rendered}"
  ]
}