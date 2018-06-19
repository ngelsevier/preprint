from fabric.operations import local


class AwsEcrDockerImageRegistry(object):
    def __init__(self, docker_registry_hostname, product, region, aws_iam_role):
        self.__docker_registry_hostname = docker_registry_hostname
        self.__product = product
        self.__region = region
        self.__aws_iam_role = aws_iam_role

    def login(self):
        self.__aws_iam_role.run(
            lambda: local("$(aws ecr get-login --region {ecr_aws_region} --no-include-email )".format(
                ecr_aws_region=self.__region))
        )

    def image_tag_for(self, service, component, version):
        return '{docker_registry_hostname}/{product}/{image_short_name}:{version}'.format(
            docker_registry_hostname=self.__docker_registry_hostname,
            product=self.__product,
            image_short_name='{service}-{component}'.format(service=service, component=component),
            version=version
        )
