
resource aws_instance automation_agent {
  count = "${var.count}"
  ami = "${var.ami_id}"
  instance_type = "${var.instance_type}"
  key_name = "${var.default_ssh_key_name}"
  vpc_security_group_ids = [
    "${var.security_group_id}",
    "${var.bastion_accessible_instance_security_group_id}",
    "${var.outbound_http_to_anywhere_security_group_id}",
    "${var.ntp_access_security_group_id}"
  ]
  subnet_id = "${element(var.private_subnet_ids, count.index)}"
  user_data = "${element(module.user_data.content_blobs, count.index)}"
  iam_instance_profile = "${var.iam_instance_profile_name}"

  root_block_device {
    delete_on_termination = true
    volume_size = "${var.volume_size_gigabytes}"
    volume_type = "gp2"
  }

  tags {
    Name = "${var.product}.${var.environment}.${var.agent_name}"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
  }
}

module user_data {
  source = "./modules/user_data_template"
  ansible_requirements_file_content_blobs = "${var.ansible_requirements_file_content_blobs}"
  ansible_playbook_file_content_blobs = "${var.ansible_playbook_file_content_blobs}"
  count = "${var.count}"
}