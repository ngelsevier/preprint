resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.service_name}"
  dashboard_body = <<EOF
 {
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${aws_ecs_service.website.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}" ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "right" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Frontend"
      }
    }
  ]
}
 EOF
}
