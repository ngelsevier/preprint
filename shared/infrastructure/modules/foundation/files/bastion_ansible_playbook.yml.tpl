---
- hosts: all
  roles:
    - aptitude_package_recipient
    - clock_synchronization_host
    - long_running_host
    - { role: aws_ssh_server, ssh_user: ${ssh_user}, ssh_user_home: ${ssh_user_home} }