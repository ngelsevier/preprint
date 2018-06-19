resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.service_name}"
  dashboard_body = <<EOF
  {
  "widgets": [
    {
      "type": "metric",
      "x": 15,
      "y": 0,
      "width": 9,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "region": "${var.aws_region}",
        "metrics": [
          [ "AWS/ES", "SearchableDocuments", "DomainName", "${aws_elasticsearch_domain.papers_index.domain_name}", "ClientId", "${data.aws_caller_identity.current.account_id}", { "period": 60, "stat": "Average", "yAxis": "left" } ],
          [ ".", "ClusterUsedSpace", ".", ".", ".", ".", { "period": 60, "stat": "Average", "yAxis": "right" } ],
          [ ".", "DeletedDocuments", ".", ".", ".", ".", { "period": 60, "stat": "Average", "yAxis": "left" } ]
        ],
        "title": "Elasticsearch Papers Index Documents",
        "period": 300,
        "yAxis": {
          "left": {
            "min": 0
          },
          "right": {
            "min": 0
          }
        }
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 9,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/Kinesis", "GetRecords.Records", "StreamName", "${data.terraform_remote_state.papers_service.kinesis_stream_name}", { "stat": "Sum", "period": 60, "yAxis": "left" } ],
          [ ".", "IncomingRecords", ".", ".", { "period": 60, "stat": "Sum" } ],
          [ ".", "GetRecords.IteratorAgeMilliseconds", ".", ".", { "period": 60, "stat": "Maximum", "yAxis": "right" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Kinesis Papers Stream",
        "period": 300,
        "yAxis": {
          "left": {
            "min": 0
          },
          "right": {
            "min": 0
          }
        }
      }
    },
    {
      "type": "metric",
      "x": 15,
      "y": 6,
      "width": 9,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ES", "JVMMemoryPressure", "DomainName", "${aws_elasticsearch_domain.papers_index.domain_name}", "ClientId", "${data.aws_caller_identity.current.account_id}", { "yAxis": "right", "stat": "p95" } ],
          [ ".", "CPUUtilization", ".", ".", ".", ".", { "stat": "p95" } ]
        ],
        "region": "${var.aws_region}",
        "yAxis": {
          "left": {
            "min": 0
          },
          "right": {
            "min": 0
          }
        },
        "title": "Elasticsearch Papers Index Resources"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 6,
      "width": 9,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/Kinesis", "GetRecords.Records", "StreamName", "${data.terraform_remote_state.authors_service.kinesis_stream_name}", { "stat": "Sum", "period": 60 } ],
          [ ".", "IncomingRecords", ".", ".", { "stat": "Sum", "period": 60 } ],
          [ ".", "GetRecords.IteratorAgeMilliseconds", ".", ".", { "yAxis": "right", "stat": "Maximum", "period": 60 } ]
        ],
        "region": "${var.aws_region}",
        "yAxis": {
          "left": {
            "min": 0
          },
          "right": {
            "min": 0
          }
        },
        "period": 300,
        "title": "Kinesis Author-Updates Stream"
      }
    },
    {
      "type": "metric",
      "x": 9,
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${aws_ecs_service.author_updates_subscriber_service.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}", { "yAxis": "left", "period": 60, "stat": "Maximum" } ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "period": 60, "yAxis": "right", "stat": "Maximum" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Author Updates Subscriber",
        "period": 300
      }
    },
    {
      "type": "metric",
      "x": 9,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${aws_ecs_service.papers_consumer.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}", { "stat": "Maximum", "period": 60 } ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "right", "stat": "Maximum", "period": 60 } ]
        ],
        "region": "${var.aws_region}",
        "yAxis": {
          "left": {
            "min": 0
          },
          "right": {
            "min": 0
          }
        },
        "title": "Papers Consumer",
        "period": 300,
        "sortDirection": {
          "arrayOfFilters": {
            "dimension.ServiceName": "asc"
          }
        }
      }
    }
  ]
}
 EOF
}
