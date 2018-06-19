from fabric.api import task
from fabric.context_managers import settings

from infrastructure.automation.functions import *


@task
def deploy(automation_agent_auto_register_key, automation_agent_root_cert_pem_content,
           automation_server_encrypted_keystore_content, aws_account_id, availability_zones, aws_region,
           contact_details,
           elsevier_cidrs, environment, global_remote_state_s3_bucket, global_remote_state_s3_key,
           global_remote_state_s3_region, public_ssh_key, vpc_cidr, dry_run='yes'):
    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'controller_deployer',
            }
        },
        'environment': environment,
        'product': 'ssrn_pipeline',
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn_pipeline-{environment}'.format(environment=environment),
                'key': 'controller.tfstate'
            },
            'variables': {
                'ansible_role_versions': terraform_command_line_map_variable_from({
                    'aptitude_package_recipient': '${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION}',
                    'automater': '${AUTOMATER_ANSIBLE_ROLE_VERSION}',
                    'automation_agent': '${AUTOMATION_AGENT_ANSIBLE_ROLE_VERSION}',
                    'automation_server': '${AUTOMATION_SERVER_ANSIBLE_ROLE_VERSION}',
                    'aws_api_client': '${AWS_API_CLIENT_ANSIBLE_ROLE_VERSION}',
                    'aws_ssh_server': '${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION}',
                    'deployer': '${DEPLOYER_ANSIBLE_ROLE_VERSION}',
                    'long_running_host': '${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION}',
                    'clock_synchronization_host': '${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION}'
                }),
                'automation_agent_auto_register_key': automation_agent_auto_register_key,
                'automation_agent_root_cert_pem_content': automation_agent_root_cert_pem_content,
                'automation_server_encrypted_keystore_content': automation_server_encrypted_keystore_content,
                'availability_zones': terraform_command_line_list_variable_from(availability_zones.split(',')),
                'aws_region': aws_region,
                'contact_details': contact_details,
                'elsevier_cidrs': terraform_command_line_list_variable_from(elsevier_cidrs.split(',')),
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'public_ssh_key': public_ssh_key,
                'vpc_cidr': vpc_cidr
            }
        }
    }

    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))


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
