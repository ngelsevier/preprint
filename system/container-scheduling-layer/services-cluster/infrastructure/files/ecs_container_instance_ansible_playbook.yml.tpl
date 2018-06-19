---
- hosts: all
  roles:
    - aptitude_package_recipient
    - long_running_host
    - clock_synchronization_host
    - { role: aws_ssh_server, ssh_user: ${ssh_user}, ssh_user_home: ${ssh_user_home} }
    - { role: aws_ecs_container_instance, ecs_cluster_name: ${ecs_cluster_name} }