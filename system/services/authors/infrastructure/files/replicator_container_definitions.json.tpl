[
  {
    "name": "service",
    "image": "${docker_image_repository_url}:${version}",
    "memoryReservation": ${memoryReservation},
    "cpu": 512,
    "essential": true,
    "environment": ${environment_variables_json_array},
    "portMappings": [
          {
            "containerPort": 8080,
            "protocol": "tcp"
          }
        ],
    "logConfiguration": {
        "logDriver": "json-file",
        "options" : {
            "max-size": "100M",
            "max-file": "40"
        }
    }
  }
]