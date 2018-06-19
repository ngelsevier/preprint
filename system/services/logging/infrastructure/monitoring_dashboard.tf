resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.service_name}"
  dashboard_body = <<EOF
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 12,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${aws_ecs_service.logstash.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}" ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "right" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Logstash Resources",
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
      "x": 12,
      "y": 0,
      "width": 12,
      "height": 12,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ES", "ClusterUsedSpace", "DomainName", "${aws_elasticsearch_domain.logging_index.domain_name}", "ClientId", "${data.aws_caller_identity.current.account_id}", { "yAxis": "right" } ],
          [ ".", "WriteThroughput", ".", ".", ".", "." ]
        ],
        "region": "${var.aws_region}",
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
    }
  ]
}
 EOF
}
