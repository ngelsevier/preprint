output kinesis_stream_name {
  value = "${aws_kinesis_stream.author_updates.name}"
}