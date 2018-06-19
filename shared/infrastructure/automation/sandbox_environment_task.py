from infrastructure.automation.aws_ecr_docker_image_registry import AwsEcrDockerImageRegistry
from infrastructure.automation.aws_iam_role import AwsIamRole
from infrastructure.automation.service import Service
from infrastructure.automation.test_environment import TestEnvironment


class SandboxEnvironmentTask(object):
    def __init__(self, environment_directory_path, offline, product, whole_system, aws_account_id=None,
                 aws_ecr_hostname=None, temporary_environment_directory_path=None, service_configurations={},
                 fake_services=[], additional_docker_compose_file_paths=[],
                 additional_docker_compose_environment_variables={}, debug=False):
        self.__services = map(
            lambda configuration: Service(
                name=configuration['name'],
                version=configuration['version'],
                components=configuration['components'],
                directory_path=configuration['directory_path']
            ),
            service_configurations.values()
        )

        self.__test_environment = TestEnvironment(
            directory_path=environment_directory_path,
            whole_system=whole_system,
            services=self.__services,
            offline=offline,
            temporary_environment_directory_path=temporary_environment_directory_path,
            docker_image_registry=AwsEcrDockerImageRegistry(
                aws_ecr_hostname, product, 'us-east-1', AwsIamRole(
                    aws_account_id, product, 'ecr_image_consumer'
                )
            ),
            fake_services=fake_services,
            aws_ecr_hostname=aws_ecr_hostname,
            product=product,
            additional_docker_compose_file_paths=additional_docker_compose_file_paths,
            additional_docker_compose_environment_variables=additional_docker_compose_environment_variables,
            debug=debug
        )

    def run(self):
        self._run(self.__test_environment, self.__services)

    def _run(self, environment, services):
        pass
