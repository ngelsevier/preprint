module foundation {
  source = "./modules/foundation"

  ansible_role_versions = "${var.ansible_role_versions}"
  ansible_role_repository_s3_bucket_domain_name = "${data.terraform_remote_state.global.global_infrastructure_state_s3_bucket_domain_name}"
  availability_zones = "${var.availability_zones}"
  bastion_iam_instance_profile = "${data.terraform_remote_state.permissions.bastion_iam_instance_profile_name}"
  contact_details = "${var.contact_details}"
  elsevier_cidrs = "${var.elsevier_cidrs}"
  environment = "${var.environment}"
  product = "${var.product}"
  public_ssh_key = "${var.public_ssh_key}"
  route_53_public_hosted_zone_id = "${data.terraform_remote_state.global.ssrn2_com_route_53_hosted_zone_id}"
  ssh_user = "${var.ssh_user}"
  ssh_user_home = "${var.ssh_user_home}"
  vpc_cidr = "${var.vpc_cidr}"
}