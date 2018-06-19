from fabric.api import task
from fabric.context_managers import shell_env

from infrastructure.automation.functions import *


@task
def build(aptitude_package_recipient_ansible_role_version, aws_ecs_container_instance_ansible_role_version,
          aws_ssh_server_ansible_role_version, long_running_host_ansible_role_version,
          clock_synchronization_host_ansible_role_version):
    with lcd(get_fabric_file_directory_path()):
        with shell_env(
                APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION=aptitude_package_recipient_ansible_role_version,
                AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION=aws_ssh_server_ansible_role_version,
                AWS_ECS_CONTAINER_INSTANCE_ANSIBLE_ROLE_VERSION=aws_ecs_container_instance_ansible_role_version,
                LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION=long_running_host_ansible_role_version,
                CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION=clock_synchronization_host_ansible_role_version,
        ):
            local('envsubst '
                  '\${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION},'
                  '\${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION},'
                  '\${AWS_ECS_CONTAINER_INSTANCE_ANSIBLE_ROLE_VERSION},'
                  '\${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION},'
                  '\${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION} '
                  '< fabfile-deployment.py > fabfile-deployment.py.rendered '
                  '&& mv fabfile-deployment.py.rendered fabfile-deployment.py')

        local('rm -rf build')
        local('mkdir -p build')
        local('tar --create --gzip --dereference --verbose --file build/deployment.tgz '
              '--transform "s|^fabfile-deployment.py$|fabfile.py|" '
              'fabfile-deployment.py infrastructure')
