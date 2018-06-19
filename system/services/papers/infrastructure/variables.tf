variable aws_region {
  type = "string"
}

variable contact_details {
  type = "string"
}

variable environment {
  type = "string"
}

variable encrypted_emitter_database_user_password {
  type = "string"
}

variable encrypted_heartbeat_database_user_password {
  type = "string"
}

variable encrypted_replicator_database_user_password {
  type = "string"
}

variable old_platform_events_feed_base_url {
  type = "string"
}

variable old_platform_events_feed_http_basic_auth_username {
  type = "string"
}

variable old_platform_events_feed_http_basic_auth_password {
  type = "string"
}

variable events_feed_max_page_request_retries {
  type = "string"
}

variable entity_feed_max_page_request_retries {
  type = "string"
}

variable global_remote_state_s3_bucket {
  type = "string"
}

variable global_remote_state_s3_key {
  type = "string"
}

variable global_remote_state_s3_region {
  type = "string"
}

variable product {
  type = "string"
}

variable replicator_memory_reservation {
  type = "string"
}

variable emitter_memory_reservation {
  type = "string"
}

variable emitter_max_concurrent_emissions {
  type = "string"
}

variable seconds_between_scheduling_replicator_jobs {
  type = "string"
}

variable replicator_entity_feed_job_batch_size {
  type = "string"
}

variable replicator_entity_feed_job_database_upsert_batch_size {
  type = "string"
}