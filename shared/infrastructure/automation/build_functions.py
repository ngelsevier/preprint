import yaml

from docker_utils import DockerUtils
from functions import *

TEMPORARY_ENVIRONMENT_DIRECTORY = '/tmp/ssrn/environment'


def create_configuration_with(aws_account_id, aws_ecr_hostname, version, service_directory_path):
    with open(os.path.join(service_directory_path, 'service.yml'), 'r') as stream:
        configuration_for_service_under_test = yaml.load(stream)

    return {
        'aws': {
            'iam_roles': {
                'ecr_image_consumer': {
                    'account_id': aws_account_id,
                    'short_name': 'ecr_image_consumer'
                },
                'ecr_image_builder': {
                    'account_id': aws_account_id,
                    'short_name': 'ecr_image_builder'
                }
            },
            'ecr': {
                'region': 'us-east-1',
                'hostname': aws_ecr_hostname
            }
        },
        'product': 'ssrn',
        'services': {
            configuration_for_service_under_test['name']: {
                'name': configuration_for_service_under_test['name'],
                'components': configuration_for_service_under_test['components'],
                'version': version,
                'testing': True,
                'sandbox_directory_path': './' if version == 'local' else TEMPORARY_ENVIRONMENT_DIRECTORY,
                'directory_path': service_directory_path
            }
        }
    }


def label_service(label, configuration):
    aws_configuration = configuration['aws']
    product = configuration['product']

    service_configuration = ConfigurationUtils.get_configuration_of_service_under_test(configuration)

    log_docker_in_to_aws_ecr(aws_configuration, product, 'ecr_image_builder')

    service_name = service_configuration['name']
    service_version = service_configuration['version']

    for component_name in service_configuration['components']:
        source_docker_image_tag = DockerUtils.get_service_component_image_tag(
            aws_configuration['ecr']['hostname'],
            product,
            service_name,
            component_name,
            service_version
        )

        DockerUtils.pull_docker_image(source_docker_image_tag)

        destination_docker_image_tag = DockerUtils.get_service_component_image_tag(
            aws_configuration['ecr']['hostname'],
            product,
            service_name,
            component_name,
            label
        )

        DockerUtils.tag_docker_image(source_docker_image_tag, destination_docker_image_tag)
        DockerUtils.push_docker_image(destination_docker_image_tag)


class ConfigurationUtils(object):
    @staticmethod
    def get_configuration_of_service_under_test(configuration):
        return next(
            service_configuration for service_name, service_configuration in configuration['services'].iteritems() if
            service_configuration['testing']
        )
