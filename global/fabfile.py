from fabric.api import task

from infrastructure.automation.functions import *


@task
def deploy(aws_region, contact_details, dry_run='yes'):
    configuration = {
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn',
                'key': 'global.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details
            }
        }
    }

    deploy_to_aws(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
