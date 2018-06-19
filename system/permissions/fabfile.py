from fabric.api import task

from infrastructure.automation.functions import *


@task
def deploy(aws_region, contact_details, environment, global_remote_state_s3_bucket, global_remote_state_s3_key, global_remote_state_s3_region,
           vpc_internal_service_hosted_zone_id, dry_run='yes'):
    configuration = {
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn-{environment}'.format(environment=environment),
                'key': 'permissions.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'vpc_internal_service_hosted_zone_id': vpc_internal_service_hosted_zone_id
            }
        }
    }

    deploy_to_aws(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))


@task
def default_deploy(environment, dry_run='yes'):
    deploy('us-east-1','m.harris@elsevier.com', environment, 'elsevier-ssrn', 'global.tfstate',
           'us-east-1','10.0.0.0/25', dry_run)

