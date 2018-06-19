variable aws_elasticsearch_port {
  type = "string"
  default = 443
}
variable aws_elasticsearch_protocol {
  type = "string"
  default = "https"
}

variable logstash_dns_name {
  type = "string"
  default = "logstash"
}

variable logstash_port {
  type = "string"
  default = 5044
}

variable logstash_name {
  type = "string"
  default = "logging-logstash"
}

variable service_name {
  type = "string"
  default = "logging"
}