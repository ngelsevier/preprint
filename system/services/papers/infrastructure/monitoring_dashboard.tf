resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.service_name}"
  dashboard_body = <<EOF
{
  "widgets": [
    {
      "type": "metric",
      "x": 12,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "metrics": [
          [ "AWS/Kinesis", "IncomingRecords", "StreamName", "${aws_kinesis_stream.papers.name}", { "stat": "Sum", "period": 60 } ],
          [ ".", "PutRecord.Latency", ".", ".", { "period": 60, "stat": "p95", "yAxis": "right" } ]
        ],
        "period": 300,
        "stat": "Sum",
        "region": "${var.aws_region}",
        "view": "timeSeries",
        "stacked": false,
        "title": "Kinesis Stream",
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
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "metrics": [
          [ "AWS/Lambda", "Invocations", "FunctionName", "${aws_lambda_function.replicator_job_event_scheduler.function_name}", { "period": 60, "yAxis": "right" } ],
          [ ".", "Errors", ".", ".", { "period": 60, "yAxis": "right" } ]
        ],
        "period": 300,
        "stat": "Sum",
        "region": "${var.aws_region}",
        "view": "timeSeries",
        "stacked": false,
        "title": "Event Replication Job Scheduler",
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
      "x": 6,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/RDS", "WriteIOPS", "DBInstanceIdentifier", "${aws_db_instance.papers.identifier}", { "period": 60, "color": "#ff7f0e" } ],
          [ ".", "WriteLatency", ".", ".", { "period": 60, "yAxis": "right", "color": "#1f77b4" } ]
        ],
        "region": "${var.aws_region}",
        "period": 300,
        "title": "Database Activity",
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
      "x": 18,
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/Lambda", "Invocations", "FunctionName", "${aws_lambda_function.papers_database_heartbeat.function_name}", { "stat": "Sum" } ],
          [ ".", "Errors", ".", ".", { "stat": "Sum" } ]
        ],
        "region": "${var.aws_region}",
        "yAxis": {
          "left": {
            "min": 0
          }
        },
        "title": "Database Heartbeat Lambda",
        "period": 300
      }
    },
    {
      "type": "metric",
      "x": 18,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/Lambda", "Invocations", "FunctionName", "${aws_lambda_function.replicator_job_paper_scheduler.function_name}", { "period": 60, "yAxis": "right" } ],
          [ ".", "Errors", ".", ".", { "stat": "Sum", "period": 60, "yAxis": "right" } ]
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
        },
        "title": "Entity Snapshots Job Scheduler"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "MemoryUtilization", "ServiceName", "${aws_ecs_service.emitter.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}", { "yAxis": "right", "stat": "Maximum" } ],
          [ ".", "CPUUtilization", ".", ".", ".", ".", { "stat": "Maximum" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Emitter Resources",
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
      "x": 6,
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "MemoryUtilization", "ServiceName", "${aws_ecs_service.replicator.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}", { "yAxis": "right", "stat": "Maximum" } ],
          [ ".", "CPUUtilization", ".", ".", ".", ".", { "stat": "Maximum" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Replicator Resources",
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
      "width": 6,
      "height": 6,
      "properties": {
        "title": "Database Replication Slot Lag",
        "annotations": {
          "alarms": [
            "arn:aws:cloudwatch:${var.aws_region}:${data.aws_caller_identity.current.account_id}:alarm:${aws_cloudwatch_metric_alarm.papers-database-oldest-replication-slot-lag-alarm.id}"
          ]
        },
        "view": "timeSeries",
        "stacked": true
      }
    }
  ]
}
 EOF
}
