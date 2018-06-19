import json
from fabric.context_managers import shell_env
from fabric.operations import local


class AwsIamRole(object):
    def __init__(self, aws_account_id, product, role_short_name, environment=None):
        self._aws_iam_role_arn = AwsIamRole.__get_role_arn(aws_account_id, environment, role_short_name, product)

    def run(self, function_to_invoke_with_iam_role):
        aws_iam_role_session_credentials = self.__get_iam_role_session_credentials()
        with shell_env(AWS_ACCESS_KEY_ID=aws_iam_role_session_credentials['AccessKeyId'],
                       AWS_SECRET_ACCESS_KEY=aws_iam_role_session_credentials['SecretAccessKey'],
                       AWS_SESSION_TOKEN=aws_iam_role_session_credentials['SessionToken']):
            function_to_invoke_with_iam_role()
        return aws_iam_role_session_credentials

    def __get_iam_role_session_credentials(self):
        command_output = local(
            'aws sts assume-role  '
            '--role-arn "{aws_iam_role_arn}" '
            '--role-session-name="$(cat /proc/sys/kernel/random/uuid)"'.format(
                aws_iam_role_arn=self._aws_iam_role_arn),
            capture=True
        )

        aws_iam_role_session = json.loads(command_output)

        return aws_iam_role_session['Credentials']

    @staticmethod
    def __get_role_arn(aws_account_id, environment, iam_role_short_name, product):
        role_name_prefix = "{product}.{environment}".format(environment=environment,
                                                            product=product) if environment else product
        aws_iam_role_arn = 'arn:aws:iam::{aws_account_id}:role/{role_name_prefix}.{short_name}'.format(
            aws_account_id=aws_account_id,
            role_name_prefix=role_name_prefix,
            short_name=iam_role_short_name)
        return aws_iam_role_arn
