ALTER USER postgres WITH ENCRYPTED PASSWORD '${master_user_password}';
ALTER USER publisher WITH ENCRYPTED PASSWORD '${publisher_user_password}';
ALTER USER replicator WITH ENCRYPTED PASSWORD '${replicator_user_password}';
ALTER USER heartbeat WITH ENCRYPTED PASSWORD '${heartbeat_user_password}';