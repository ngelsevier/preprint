---
- hosts: all
  roles:
    - aptitude_package_recipient
    - clock_synchronization_host
    - long_running_host
    - aws_api_client
    - { role: aws_ssh_server, ssh_user: ${ssh_user}, ssh_user_home: ${ssh_user_home} }
    - automater
    - { role: container_factory, docker_user: go }
    - { role: deployer, additional_software_directory: /opt, local_software_directory: /usr/local/bin }
    - { role: automation_agent, automation_server_hostname: ${automation_server_hostname}, automation_server_https_port: ${automation_server_https_port}, agent_auto_register_key: ${agent_auto_register_key}, agent_environments: "${agent_environments}", agent_resources: "${agent_resources}", root_cert_pem_content: "${agent_root_cert_pem_content}", verify_server_hostname_against_root_certificate: false }
