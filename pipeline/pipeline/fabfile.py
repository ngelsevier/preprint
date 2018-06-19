from fabric.api import settings
from fabric.context_managers import shell_env

from infrastructure.automation.functions import *


@task
def build(aptitude_package_recipient_ansible_role_version, automater_ansible_role_version,
          automation_agent_ansible_role_version, automation_server_ansible_role_version,
          aws_api_client_ansible_role_version, aws_ssh_server_ansible_role_version, deployer_ansible_role_version,
          container_factory_ansible_role_version, frontend_project_builder_ansible_role_version,
          ssrn_system_simulator_ansible_role_version, java_project_builder_ansible_role_version,
          long_running_host_ansible_role_version, browser_test_runner_ansible_role_version,
          clock_synchronization_host_ansible_role_version):
    with lcd(get_fabric_file_directory_path()):
        with shell_env(
                APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION=aptitude_package_recipient_ansible_role_version,
                AUTOMATER_ANSIBLE_ROLE_VERSION=automater_ansible_role_version,
                AUTOMATION_AGENT_ANSIBLE_ROLE_VERSION=automation_agent_ansible_role_version,
                AUTOMATION_SERVER_ANSIBLE_ROLE_VERSION=automation_server_ansible_role_version,
                AWS_API_CLIENT_ANSIBLE_ROLE_VERSION=aws_api_client_ansible_role_version,
                AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION=aws_ssh_server_ansible_role_version,
                DEPLOYER_ANSIBLE_ROLE_VERSION=deployer_ansible_role_version,
                CONTAINER_FACTORY_ANSIBLE_ROLE_VERSION=container_factory_ansible_role_version,
                FRONTEND_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION=frontend_project_builder_ansible_role_version,
                SSRN_SYSTEM_SIMULATOR_ANSIBLE_ROLE_VERSION=ssrn_system_simulator_ansible_role_version,
                JAVA_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION=java_project_builder_ansible_role_version,
                LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION=long_running_host_ansible_role_version,
                BROWSER_TEST_RUNNER_ANSIBLE_ROLE_VERSION=browser_test_runner_ansible_role_version,
                CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION=clock_synchronization_host_ansible_role_version
        ):
            local('envsubst '
                  '\${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION},'
                  '\${AUTOMATER_ANSIBLE_ROLE_VERSION},'
                  '\${AUTOMATION_AGENT_ANSIBLE_ROLE_VERSION},'
                  '\${AUTOMATION_SERVER_ANSIBLE_ROLE_VERSION},'
                  '\${AWS_API_CLIENT_ANSIBLE_ROLE_VERSION},'
                  '\${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION},'
                  '\${DEPLOYER_ANSIBLE_ROLE_VERSION},'
                  '\${CONTAINER_FACTORY_ANSIBLE_ROLE_VERSION},'
                  '\${FRONTEND_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION},'
                  '\${SSRN_SYSTEM_SIMULATOR_ANSIBLE_ROLE_VERSION},'
                  '\${JAVA_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION},'
                  '\${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION},'
                  '\${BROWSER_TEST_RUNNER_ANSIBLE_ROLE_VERSION},'
                  '\${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION} '
                  '< fabfile-deployment.py > fabfile-deployment.py.rendered '
                  '&& mv fabfile-deployment.py.rendered fabfile-deployment.py')

        local('rm -rf build')
        local('mkdir -p build')
        local('tar --create --gzip --dereference --verbose --file build/deployment.tgz '
              '--transform "s|^fabfile-deployment.py$|fabfile.py|" '
              'fabfile-deployment.py infrastructure')


@task
def remove_old_docker_images():
    local('docker container prune --force')
    local('docker image prune --force')

    ssrn_service_repository_prefix = '/ssrn/'

    newest_ssrn_service_images_regex_pattern = local(
        'docker image ls --format "{{{{.Tag}}}} {{{{.ID}}}} {{{{.Repository}}}}" | '
        'grep "{ssrn_service_repository_prefix}" | '
        'grep -E "^v[0-9]+\.[0-9]+\.[0-9]+ " | '
        'sed -r "s/^v([0-9]+)\.([0-9]+)\.([0-9]+)/\\1 \\2 \\3/" | '
        'sort -k5,5 -k1,1nr -k2,2nr -k3,3nr | '
        'uniq -f 4 | '
        'cut -d " " -f4 | '
        'xargs echo -n | '
        'tr " " "|"'.format(ssrn_service_repository_prefix=ssrn_service_repository_prefix),
        capture=True
    )

    if not newest_ssrn_service_images_regex_pattern:
        return

    images_to_delete = local(
        'docker image ls --format \"{{{{.Tag}}}} {{{{.ID}}}} {{{{.Repository}}}}\" | '
        'grep "{ssrn_service_repository_prefix}" | '
        'grep -E "^v[0-9]+\.[0-9]+\.[0-9]+ " | cut -d " " -f2 | '
        'grep -v -E -e "{newest_ssrn_service_images_regex_pattern}" | '
        'sort -u |'
        'xargs echo -n'.format(
            ssrn_service_repository_prefix=ssrn_service_repository_prefix,
            newest_ssrn_service_images_regex_pattern=newest_ssrn_service_images_regex_pattern
        ),
        capture=True
    )

    if not images_to_delete:
        return

    with settings(warn_only=True):
        local('docker image rm {images_to_delete}'.format(images_to_delete=images_to_delete))
