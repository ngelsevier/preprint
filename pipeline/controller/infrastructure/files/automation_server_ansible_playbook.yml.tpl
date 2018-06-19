---
- hosts: all
  roles:
    - aptitude_package_recipient
    - clock_synchronization_host
    - { role: aws_ssh_server, ssh_user: ${ssh_user}, ssh_user_home: ${ssh_user_home} }
    - long_running_host
    - { role: automation_server, automation_server_http_port: ${automation_server_http_port}, automation_server_https_port: ${automation_server_https_port}, automation_server_backup_s3_bucket_name: ${automation_server_backup_s3_bucket_name}, automation_server_backup_s3_key_prefix: ${automation_server_backup_s3_key_prefix}, automation_server_comma_separated_users_list: "${automation_server_comma_separated_users_list}", kms_region: "${automation_server_kms_region}", encrypted_keystore_content: "${automation_server_encrypted_keystore_content}" }