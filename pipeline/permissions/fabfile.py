from fabric.api import task

from infrastructure.automation.functions import *


@task
def deploy(aws_region, contact_details, environment, global_remote_state_s3_bucket, global_remote_state_s3_key,
           global_remote_state_s3_region, dry_run='yes'):
    configuration = {
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn_pipeline-{environment}'.format(environment=environment),
                'key': 'permissions.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region
            }
        }
    }

    deploy_to_aws(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
