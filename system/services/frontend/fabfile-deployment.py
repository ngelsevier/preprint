from fabric.api import task
import json

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
                'key': 'services/frontend.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'encrypted_frontend_website_password': extended_configuration_dictionary[
                    'encryptedFrontendWebsitePassword'
                ],
                'encrypted_frontend_website_username': extended_configuration_dictionary[
                    'encryptedFrontendWebsiteUsername'
                ],
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'instance_count': extended_configuration_dictionary['websiteInstanceCount'],
                'old_platform_article_page_base_url': extended_configuration_dictionary[
                    'oldPlatformArticlePageBaseUrl'
                ],
                'old_platform_author_profile_page_base_url': extended_configuration_dictionary[
                    'oldPlatformAuthorProfilePageBaseUrl'
                ],
                'old_platform_author_image_base_url': extended_configuration_dictionary[
                    'oldPlatformAuthorImageBaseUrl'
                ],
                'old_platform_auth_base_url': extended_configuration_dictionary[
                    'oldPlatformAuthBaseUrl'
                ],
                'frontend_healthcheck_lambda_enabled': extended_configuration_dictionary[
                    'frontendHealthcheckLambdaEnabled'
                ],
                'product': product,
                'search_result_page_size': extended_configuration_dictionary[
                    'searchResultPageSize'
                ],
            }
        }
    }

    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
