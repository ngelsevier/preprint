from fabric.api import task

from infrastructure.automation.functions import *


@task
def deploy(aws_account_id, aws_region, contact_details, environment, global_remote_state_s3_bucket,
           global_remote_state_s3_key, global_remote_state_s3_region, instances_per_subnet, dry_run='yes'):
    product = 'ssrn'
    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'container_scheduling_layer_deployer',
            }
        },
        'environment': environment,
        'product': product,
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn-{environment}'.format(environment=environment),
                'key': 'container-scheduling-layer/services-cluster.tfstate'
            },
            'variables': {
                'ansible_role_versions': terraform_command_line_map_variable_from({
                    'aptitude_package_recipient': '${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION}',
                    'aws_ecs_container_instance': '${AWS_ECS_CONTAINER_INSTANCE_ANSIBLE_ROLE_VERSION}',
                    'aws_ssh_server': '${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION}',
                    'long_running_host': '${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION}',
                    'clock_synchronization_host': '${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION}'
                }),
                'aws_region': aws_region,
                'cluster_name': 'services',
                'contact_details': contact_details,
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'instances_per_subnet': instances_per_subnet,
                'product': product
            }
        }
    }

    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
