from base64 import b64decode
from fabric.api import task
from fabric.context_managers import lcd
from fabric.state import env

import boto3

from aws_ecr_docker_image_registry import AwsEcrDockerImageRegistry
from aws_iam_role import AwsIamRole
from tasks import *


@task
def define_infrastructure_version(version):
    with lcd(__get_infrastructure_definitions_directory()):
        local(
            "find -L -path ./.terraform -prune -o -type f -name '*.tf' "
            "-exec sed -i 's/__VERSION__/{version}/g' {{}} \; "
            "-exec sed -i 's/__ESCAPED_VERSION__/{escaped_version}/g' {{}} \;".format(
                version=version,
                escaped_version=version.replace('.', '_')
            )
        )


def deploy_to_aws(configuration, dry_run):
    with lcd(__get_infrastructure_definitions_directory()):
        if dry_run:
            preview_infrastructure_changes(configuration)
        else:
            apply_infrastructure_changes(configuration)


def deploy_to_aws_with_iam_role(configuration, dry_run):
    return with_iam_role(
        lambda: deploy_to_aws(configuration, dry_run),
        configuration['product'],
        configuration['aws']['iam_role'],
        configuration['environment']
    )


def with_iam_role(function_to_invoke_with_iam_role, product, iam_role, environment=None):
    aws_iam_role = AwsIamRole(iam_role['account_id'], product, iam_role['short_name'], environment)
    return aws_iam_role.run(function_to_invoke_with_iam_role)


def terraform_command_line_list_variable_from(list):
    return '[{}]'.format(','.join(map(lambda value: '"{}"'.format(value), list)))


def terraform_command_line_map_variable_from(dictionary):
    return '{{ {key_pairs_string} }}'.format(key_pairs_string=', '.join(
        map(lambda (key, value): '{key} = "{value}"'.format(key=key, value=value), dictionary.iteritems()))
    )


def in_fabfile_directory(function_to_invoke_in_fabfile_directory):
    with lcd(get_fabric_file_directory_path()):
        function_to_invoke_in_fabfile_directory()


def using_kms_decrypt_or_default(encrypted_text, default_value, aws_iam_role_session_credentials, aws_region):
    if not encrypted_text:
        return default_value

    return __using_kms_decrypt(
        encrypted_text,
        aws_iam_role_session_credentials,
        aws_region
    )


def kms_encrypt_and_base64_encode_content_in(unencrypted_file_path, aws_region, kms_master_key_arn):
    return local(
        'aws kms encrypt '
        '--key-id={kms_master_key_arn} '
        '--plaintext=fileb://{unencrypted_text_file_path} '
        '--output text '
        '--query CiphertextBlob '
        '--region={aws_region}'.format(
            unencrypted_text_file_path=unencrypted_file_path,
            kms_master_key_arn=kms_master_key_arn,
            aws_region=aws_region
        ),
        capture=True
    )


def __using_kms_decrypt(encrypted_text, aws_iam_role_session_credentials, aws_region):
    client = boto3.client(
        'kms',
        aws_access_key_id=aws_iam_role_session_credentials['AccessKeyId'],
        aws_secret_access_key=aws_iam_role_session_credentials['SecretAccessKey'],
        aws_session_token=aws_iam_role_session_credentials['SessionToken'],
        region_name=aws_region
    )
    decrypt_response = client.decrypt(CiphertextBlob=(b64decode(encrypted_text)))
    return decrypt_response['Plaintext']


def __get_infrastructure_definitions_directory():
    return os.path.join(get_fabric_file_directory_path(), 'infrastructure')


def get_fabric_file_directory_path():
    return os.path.dirname(env.real_fabfile)


def log_docker_in_to_aws_ecr(aws_configuration, product, role):
    iam_role_configuration = aws_configuration['iam_roles'][role]
    iam_role = AwsIamRole(iam_role_configuration['account_id'], product, iam_role_configuration['short_name'])
    ecr_configuration = aws_configuration['ecr']
    docker_image_registry = AwsEcrDockerImageRegistry(
        ecr_configuration['hostname'],
        product,
        ecr_configuration['region'],
        iam_role
    )

    docker_image_registry.login()


def yes_indicated_by(text):
    return text.lower() in ['y', 'yes']
