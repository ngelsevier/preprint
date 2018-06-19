from fabric.api import task

from infrastructure.automation.functions import *


@task
def deploy(availability_zones, automation_agent_auto_register_key, automation_agent_root_cert_pem_content,
           automation_server_hostname, aws_account_id, aws_region, contact_details, elasticsearch_endpoint, elsevier_cidrs, environment, global_remote_state_s3_bucket,
           global_remote_state_s3_key, global_remote_state_s3_region, public_ssh_key, vpc_cidr, dry_run='yes'):
    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'foundational_infrastructure_deployer',
            }
        },
        'environment': environment,
        'product': 'ssrn',
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn-{environment}'.format(environment=environment),
                'key': 'vpc.tfstate'
            },
            'variables': {
                'ansible_role_versions': terraform_command_line_map_variable_from({
                    'aptitude_package_recipient': '${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION}',
                    'automater': '${AUTOMATER_ANSIBLE_ROLE_VERSION}',
                    'automation_agent': '${AUTOMATION_AGENT_ANSIBLE_ROLE_VERSION}',
                    'aws_api_client': '${AWS_API_CLIENT_ANSIBLE_ROLE_VERSION}',
                    'aws_elasticsearch_client': '${AWS_ELASTICSEARCH_CLIENT_ANSIBLE_ROLE_VERSION}',
                    'aws_ssh_server': '${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION}',
                    'database_administrator': '${DATABASE_ADMINISTRATOR_ANSIBLE_ROLE_VERSION}',
                    'deployer': '${DEPLOYER_ANSIBLE_ROLE_VERSION}',
                    'long_running_host': '${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION}',
                    'clock_synchronization_host': '${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION}'
                }),
                'availability_zones': terraform_command_line_list_variable_from(availability_zones.split(',')),
                'automation_agent_auto_register_key': automation_agent_auto_register_key,
                'automation_agent_root_cert_pem_content': automation_agent_root_cert_pem_content,
                'automation_server_hostname': automation_server_hostname,
                'aws_region': aws_region,
                'contact_details': contact_details,
                'elasticsearch_endpoint': elasticsearch_endpoint,
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
