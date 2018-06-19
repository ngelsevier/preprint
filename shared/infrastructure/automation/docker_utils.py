from fabric.api import local

from aws_ecr_docker_image_registry import AwsEcrDockerImageRegistry


class DockerUtils(object):
    @staticmethod
    def tag_docker_image(docker_image_tag, labelled_docker_image_tag):
        local('docker image tag "{source_docker_image_tag}" "{destination_docker_image_tag}"'.format(
            source_docker_image_tag=docker_image_tag,
            destination_docker_image_tag=labelled_docker_image_tag
        ))

    @staticmethod
    def build_docker_image(docker_image_tag, commit_hash, version, docker_file_directory_path, parent_image_version):
        local('docker image build '
              '--pull '
              '--build-arg commit="{commit_hash}" '
              '--build-arg version="{version}" '
              '--build-arg parent_image_version="{parent_image_version}" '
              '--tag "{docker_image_tag}" '
              '"{docker_file_directory_path}"'.format(version=version,
                                                      docker_image_tag=docker_image_tag,
                                                      docker_file_directory_path=docker_file_directory_path,
                                                      parent_image_version=parent_image_version,
                                                      commit_hash=commit_hash)
              )

    @staticmethod
    def push_docker_image(docker_image_tag):
        local('docker image push "{docker_image_tag}"'.format(docker_image_tag=docker_image_tag))

    @staticmethod
    def pull_docker_image(docker_image_tag):
        local('docker image pull "{docker_image_tag}"'.format(docker_image_tag=docker_image_tag))

    @staticmethod
    def get_service_component_image_tag(docker_registry_hostname, product, service, component, version):
        return AwsEcrDockerImageRegistry(docker_registry_hostname, product, None, None).image_tag_for(service,
                                                                                                      component,
                                                                                                      version)
