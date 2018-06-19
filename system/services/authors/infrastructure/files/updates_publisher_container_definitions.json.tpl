[
  {
    "name": "service",
    "image": "${docker_image_repository_url}:${version}",
    "memoryReservation": ${memoryReservation},
    "cpu": 1024,
    "essential": true,
    "environment": ${environment_variables_json_array},
    "logConfiguration": {
        "logDriver": "json-file",
        "options" : {
            "max-size": "10M",
            "max-file": "10"
        }
    }
  }
]