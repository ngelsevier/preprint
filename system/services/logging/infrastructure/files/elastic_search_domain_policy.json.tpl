{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": "es:ESHttp*",
      "Resource": "${elastic_search_domain}",
      "Condition": {
        "IpAddress": {
          "aws:SourceIp": ${whitelist_ips}
        }
      }
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${logstash_iam_role}"
      },
      "Action": [
        "es:ESHttpPost",
        "es:ESHttpPut",
        "es:ESHttpHead"
      ],
      "Resource": "${elastic_search_domain}"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${logging_janitor_iam_role}"
      },
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpHead",
        "es:ESHttpDelete"
      ],
      "Resource": "${elastic_search_domain}"
    },
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${database_admin_iam_role}"
      },
      "Action": [
        "es:ESHttp*"
      ],
      "Resource": "${elastic_search_domain}"
    }
  ]
}