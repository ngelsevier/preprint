data aws_ami default {
  most_recent = true

  owners = [
    "099720109477",
  ]

  filter {
    name = "architecture"

    values = [
      "x86_64",
    ]
  }

  filter {
    name = "image-type"

    values = [
      "machine",
    ]
  }

  filter {
    name = "state"

    values = [
      "available",
    ]
  }

  filter {
    name = "virtualization-type"

    values = [
      "hvm",
    ]
  }

  filter {
    name = "root-device-type"

    values = [
      "ebs",
    ]
  }

  filter {
    name = "block-device-mapping.volume-type"

    values = [
      "gp2",
    ]
  }

  filter {
    name = "name"

    values = [
      "*ubuntu-xenial-16.04-amd64-server-*",
    ]
  }
}
