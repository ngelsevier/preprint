data template_file user_data {
  template = "${file("${path.module}/files/user_data.sh.tpl")}"
  count = "${var.count}"

  vars {
    ansible_requirements_file_content = "${element(var.ansible_requirements_file_content_blobs, count.index)}"
    ansible_playbook_file_content = "${element(var.ansible_playbook_file_content_blobs, count.index)}"
  }
}