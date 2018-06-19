import json
from fabric.api import task

from infrastructure.automation.build_functions import *


@task
def label(aws_account_id, aws_ecr_hostname, label):
    service_directory_path = get_fabric_file_directory_path()

    label_service(
        label=label,
        configuration=create_configuration_with(
            aws_account_id,
            aws_ecr_hostname,
            '${VERSION}',
            service_directory_path
        )
    )


@task
def deploy(aws_account_id, aws_region, contact_details, environment, extended_configuration,
           global_remote_state_s3_bucket, global_remote_state_s3_key, global_remote_state_s3_region, dry_run='yes'):
    extended_configuration_dictionary = json.loads(extended_configuration)
    product = 'ssrn'

    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'service_deployer',
            }
        },
        'environment': environment,
        'product': product,
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn-{environment}'.format(environment=environment),
                'key': 'services/logging.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'elsevier_cidrs': terraform_command_line_list_variable_from(
                    extended_configuration_dictionary['elsevierCidrs'].split(',')),
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'logstash_instance_count': extended_configuration_dictionary['logstashInstanceCount'],
                'logging_cleanup_threshold_in_days': extended_configuration_dictionary['loggingCleanupThresholdInDays'],
                'logging_cleanup_access_log_threshold_in_days': extended_configuration_dictionary['loggingCleanupAccessLogThresholdInDays'],
                'product': product
            }
        }
    }
    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
