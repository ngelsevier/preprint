[
  {
    "name": "service",
    "image": "${docker_image_repository_url}:${version}",
    "memoryReservation": 2048,
    "cpu": 512,
    "essential": true,
    "environment": ${environment_variables_json_array},
    "portMappings": [
        {
            "hostPort": ${logstash_port},
            "containerPort": 5044,
            "protocol": "tcp"
        }
    ]
  }
]