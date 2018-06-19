import os
import yaml
from fabric.context_managers import lcd, shell_env
from fabric.operations import local

from infrastructure.automation.aws_ecr_docker_image_registry import AwsEcrDockerImageRegistry
from infrastructure.automation.aws_iam_role import AwsIamRole
from infrastructure.automation.docker_utils import DockerUtils
from infrastructure.automation.functions import define_infrastructure_version
from infrastructure.automation.gradle_docker_context_generator import GradleDockerContextGenerator


class BuildServiceTask(object):
    def __init__(self, aws_account_id, aws_ecr_hostname, version, commit_hash, parent_image_version,
                 service_directory_path):
        self.__aws_account_id = aws_account_id
        self.__aws_ecr_hostname = aws_ecr_hostname
        self.__commit_hash = commit_hash
        self.__parent_image_version = parent_image_version
        self.__version = version
        self.__service_directory_path = service_directory_path

        with open(os.path.join(self.__service_directory_path, 'service.yml'), 'r') as stream:
            service_configuration = yaml.load(stream)

        self.__components = service_configuration['components']
        self.__service_name = service_configuration['name']

    def run(self):
        with lcd(self.__service_directory_path):
            local('gradle clean check')

        GradleDockerContextGenerator.generate_docker_contexts_for(
            self.__components,
            service_directory_path=self.__service_directory_path
        )

        product = 'ssrn'

        docker_image_registry = AwsEcrDockerImageRegistry(
            self.__aws_ecr_hostname, product, 'us-east-1', AwsIamRole(
                self.__aws_account_id, product, 'ecr_image_builder'
            )
        )

        with lcd(self.__service_directory_path):
            docker_image_registry.login()

            for component_name in self.__components:
                docker_image_tag = docker_image_registry.image_tag_for(
                    self.__service_name, component_name,
                    self.__version
                )
                DockerUtils.build_docker_image(
                    docker_image_tag, self.__commit_hash, self.__version, component_name,
                    self.__parent_image_version
                )
                DockerUtils.push_docker_image(docker_image_tag)

        with lcd(self.__service_directory_path):
            local('mkdir -p build')

            define_infrastructure_version(self.__version)

            with shell_env(VERSION=self.__version):
                local('envsubst \${VERSION} < fabfile-deployment.py > fabfile-deployment.py.rendered '
                      '&& mv fabfile-deployment.py.rendered fabfile-deployment.py')

            additional_python_modules_directory_relative_path = 'automation'
            additional_python_modules_directory_absolute_path = os.path.join(
                self.__service_directory_path,
                additional_python_modules_directory_relative_path
            )

            local(
                'tar --create --gzip --dereference --verbose --file build/deployment.tgz '
                '--transform "s|^fabfile-deployment.py$|fabfile.py|" '
                'fabfile-deployment.py infrastructure service.yml {additional_file_paths}'.format(
                    additional_file_paths=additional_python_modules_directory_relative_path
                    if os.path.exists(additional_python_modules_directory_absolute_path) else ''
                )
            )

            with lcd('build'):
                local('mkdir -p contract-tests')

                with lcd('contract-tests'):
                    local(
                        'tar --create --dereference --verbose --file ../contract-tests.tar .'
                    )
