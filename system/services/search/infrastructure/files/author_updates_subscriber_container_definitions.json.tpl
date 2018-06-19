[
  {
    "name": "service",
    "image": "${docker_image_repository_url}:${version}",
    "memoryReservation": 2048,
    "cpu": 512,
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