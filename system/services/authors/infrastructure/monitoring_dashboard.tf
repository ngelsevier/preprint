resource aws_cloudwatch_dashboard monitoring {
  dashboard_name = "${var.product}-${var.environment}-${var.service_name}"
  dashboard_body = <<EOF
 {
  "widgets": [
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
          [ "AWS/RDS", "WriteIOPS", "DBInstanceIdentifier", "${aws_db_instance.authors.identifier}", { "yAxis": "left", "period": 60 } ],
          [ ".", "WriteLatency", ".", ".", { "yAxis": "right", "period": 60 } ]
        ],
        "region": "${var.aws_region}",
        "period": 300,
        "title": "Database Activity"
      }
    },
    {
      "type": "metric",
      "x": 12,
      "y": 0,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/Kinesis", "IncomingRecords", "StreamName", "${aws_kinesis_stream.author_updates.name}", { "stat": "Sum", "period": 60 } ],
          [ ".", "PutRecord.Latency", ".", ".", { "stat": "p95", "yAxis": "right", "period": 60 } ]
        ],
        "region": "${var.aws_region}",
        "period": 300,
        "title": "Kinesis Stream",
        "yAxis": {
          "left": {
            "min": 0
          }
        }
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
          [ "AWS/Lambda", "Invocations", "FunctionName", "${aws_lambda_function.replicator_job_author_scheduler.function_name}", { "period": 60, "yAxis": "right" } ],
          [ ".", "Errors", ".", ".", { "period": 60, "yAxis": "right" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Entities Job Scheduler"
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
          [ "AWS/Lambda", "Errors", "FunctionName", "${aws_lambda_function.authors_database_heartbeat.function_name}", "Resource", "${aws_lambda_function.authors_database_heartbeat.function_name}", { "stat": "Sum" } ],
          [ ".", "Invocations", ".", ".", ".", ".", { "stat": "Sum" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Database Heartbeat Lambda",
        "period": 300,
        "yAxis": {
          "left": {
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
            "arn:aws:cloudwatch:${var.aws_region}:${data.aws_caller_identity.current.account_id}:alarm:${aws_cloudwatch_metric_alarm.authors-database-oldest-replication-slot-lag-alarm.id}"
          ]
        },
        "view": "timeSeries",
        "stacked": true
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
          [ "AWS/Lambda", "Invocations", "FunctionName", "${aws_lambda_function.replicator_job_event_scheduler.function_name}", "Resource", "${aws_lambda_function.replicator_job_event_scheduler.function_name}", { "period": 60, "stat": "Sum", "yAxis": "right" } ],
          [ ".", "Errors", ".", ".", ".", ".", { "period": 60, "stat": "Sum" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Events Job Scheduler",
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
      "y": 6,
      "width": 6,
      "height": 6,
      "properties": {
        "view": "timeSeries",
        "stacked": false,
        "metrics": [
          [ "AWS/ECS", "CPUUtilization", "ServiceName", "${aws_ecs_service.updates_publisher.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}" ],
          [ ".", "MemoryUtilization", ".", ".", ".", ".", { "yAxis": "right" } ]
        ],
        "region": "${var.aws_region}",
        "title": "Updates Publisher"
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
          [ "AWS/ECS", "MemoryUtilization", "ServiceName", "${aws_ecs_service.authors_replicator.name}", "ClusterName", "${data.terraform_remote_state.ecs_cluster.cluster_name}" ],
          [ ".", "CPUUtilization", ".", ".", ".", "." ]
        ],
        "region": "${var.aws_region}",
        "title": "Replicator"
      }
    }
  ]
}
 EOF
}
