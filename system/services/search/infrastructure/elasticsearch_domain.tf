resource aws_elasticsearch_domain papers_index {
  domain_name = "${format("%s-papers-index",  var.environment)}"

  cluster_config {
    instance_type = "m3.large.elasticsearch"
    instance_count = 2
    dedicated_master_enabled = true
    dedicated_master_type = "m3.medium.elasticsearch"
    dedicated_master_count = 3
    zone_awareness_enabled = false
  }

  snapshot_options {
    automated_snapshot_start_hour = 0
  }

  elasticsearch_version = "5.3"

  lifecycle {
    prevent_destroy = true
    ignore_changes = [
      "*"]
  }

  tags {
    Name = "${var.product}.${var.environment}.papers-index"
    contact = "${var.contact_details}"
    environment = "${var.environment}"
    product = "${var.product}"
    service = "${var.service_name}"
  }
}

resource aws_elasticsearch_domain_policy papers_index {

  domain_name = "${aws_elasticsearch_domain.papers_index.domain_name}"

  access_policies = <<POLICIES
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${data.terraform_remote_state.permissions.search_api_iam_role_arn}"
      },
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpHead"
      ],
      "Resource": "${aws_elasticsearch_domain.papers_index.arn}/*"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${data.terraform_remote_state.permissions.search_papers_consumer_iam_role_arn}"
      },
      "Action": [
        "es:ESHttp*"
      ],
      "Resource": "${aws_elasticsearch_domain.papers_index.arn}/*"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${data.terraform_remote_state.permissions.search_author_updates_subscriber_iam_role_arn}"
      },
      "Action": [
        "es:ESHttp*"
      ],
      "Resource": "${aws_elasticsearch_domain.papers_index.arn}/*"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${data.terraform_remote_state.permissions.database_administrator_iam_role_arn}"
      },
      "Action": [
        "es:ESHttp*"
      ],
      "Resource": "${aws_elasticsearch_domain.papers_index.arn}/*"
    }
  ]
}
POLICIES
}