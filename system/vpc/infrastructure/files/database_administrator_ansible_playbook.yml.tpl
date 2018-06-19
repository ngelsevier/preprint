---
- hosts: all
  roles:
    - aptitude_package_recipient
    - clock_synchronization_host
    - database_administrator
    - long_running_host
    - { role: aws_elasticsearch_client, elasticsearch_endpoint: "${elasticsearch_endpoint}" }
    - { role: aws_ssh_server, ssh_user: ${ssh_user}, ssh_user_home: ${ssh_user_home} }
