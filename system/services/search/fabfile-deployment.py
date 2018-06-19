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
                'key': 'services/search.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'elasticsearch_scroll_size': extended_configuration_dictionary['elasticSearchScrollSize'],
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'instance_count': extended_configuration_dictionary['apiInstanceCount'],
                'product': product
            }
        }
    }

    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
