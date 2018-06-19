resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.cluster_name}-cluster"
  dashboard_body = <<EOF
  {
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 24,
      "height": 12,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "region": "${var.aws_region}",
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${var.product}-${var.environment}-frontend-website", "ClusterName", "${aws_ecs_cluster.cluster.name}", { "period": 60, "stat": "Maximum" } ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "left", "period": 60, "stat": "Maximum" } ],
          [ "...", "${var.product}-${var.environment}-search-author-updates-subscriber", ".", ".", { "period": 60, "stat": "Maximum", "yAxis": "left" } ],
          [ "...", "${var.product}-${var.environment}-search-papers-consumer", ".", ".", { "period": 60, "stat": "Maximum", "yAxis": "left" } ],
          [ "...", "${var.product}-${var.environment}-search-api", ".", ".", { "period": 60, "stat": "Maximum", "yAxis": "left" } ],
          [ ".", "CPUUtilization", ".", "${var.product}-${var.environment}-search-author-updates-subscriber", ".", ".", { "stat": "Maximum", "period": 60 } ],
          [ "...", "${var.product}-${var.environment}-search-papers-consumer", ".", ".", { "stat": "Maximum", "period": 60 } ],
          [ "...", "${var.product}-${var.environment}-search-api", ".", ".", { "stat": "Maximum", "period": 60 } ],
          [ "...", "${var.product}-${var.environment}-papers-replicator", ".", ".", { "period": 60, "stat": "Maximum" } ],
          [ "...", "${var.product}-${var.environment}-papers-emitter", ".", ".", { "period": 60, "stat": "Maximum" } ],
          [ ".", "MemoryUtilization", ".", "${var.product}-${var.environment}-papers-replicator", ".", ".", { "yAxis": "left", "stat": "Maximum", "period": 60 } ],
          [ "...", "${var.product}-${var.environment}-papers-emitter", ".", ".", { "yAxis": "left", "stat": "Maximum", "period": 60 } ],
          [ "...", "${var.product}-${var.environment}-authors-updates-publisher", ".", ".", { "yAxis": "left", "period": 60, "stat": "Maximum" } ],
          [ ".", "CPUUtilization", ".", ".", ".", ".", { "period": 60, "stat": "Maximum" } ],
          [ "...", "${var.product}-${var.environment}-authors-replicator", ".", ".", { "period": 60, "stat": "Maximum" } ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "left", "period": 60, "stat": "Maximum" } ],
          [ "...", "${var.product}-${var.environment}-logging-logstash", ".", ".", { "period": 60, "stat": "Maximum" } ],
          [ ".", "CPUUtilization", ".", ".", ".", ".", { "period": 60, "stat": "Maximum" } ]
        ],
        "sortDirection": {
          "arrayOfFilters": {
            "action": "desc"
          }
        },
        "period": 300,
        "annotations": {
          "horizontal": [
            {
              "label": "Reservation",
              "value": 100,
              "fill": "above"
            }
          ]
        },
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
