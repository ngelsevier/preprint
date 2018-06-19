resource aws_elasticsearch_domain logging_index {
  domain_name = "${format("%s-logging-index", var.environment)}"

  cluster_config {
    instance_type = "m4.large.elasticsearch"
    instance_count = 2
    dedicated_master_enabled = true
    dedicated_master_type = "m3.medium.elasticsearch"
    dedicated_master_count = 3
    zone_awareness_enabled = false
  }

  ebs_options {
    ebs_enabled = true
    volume_type = "gp2"
    volume_size = 200
  }

  snapshot_options {
    automated_snapshot_start_hour = 0
  }

  elasticsearch_version = "5.3"

  lifecycle {
    prevent_destroy = true
  }

  tags {
    Name = "${var.product}.${var.environment}.logging-index"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}


resource aws_elasticsearch_domain_policy logging_index {

  domain_name = "${aws_elasticsearch_domain.logging_index.domain_name}"
  access_policies = "${data.template_file.elastic_search_domain_policy.rendered}"
}

data template_file elastic_search_domain_policy {
  template = "${file("${path.module}/files/elastic_search_domain_policy.json.tpl")}"

  vars {
    whitelist_ips = "${jsonencode(var.elsevier_cidrs)}"
    elastic_search_domain = "${aws_elasticsearch_domain.logging_index.arn}/*"
    logging_janitor_iam_role = "${data.terraform_remote_state.permissions.logging_janitor_lambda_executor_iam_role_arn}"
    logstash_iam_role = "${data.terraform_remote_state.permissions.logging_logstash_iam_role_arn}"
    database_admin_iam_role = "${data.terraform_remote_state.permissions.database_administrator_iam_role_arn}"
  }
}