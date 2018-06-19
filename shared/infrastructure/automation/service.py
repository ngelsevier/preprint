from fabric.api import local


class Service(object):
    def __init__(self, name, version, components, directory_path):
        self.__name = name
        self.__components = components
        self.__version = version
        self.__directory_path = directory_path

    def name(self):
        return self.__name

    def version(self):
        return self.__version

    def is_local_version(self):
        return self.__version == 'local'

    def components(self):
        return self.__components

    def directory_path(self):
        return self.__directory_path

    def configure_test_environment(self):
        local('fab -f {service_directory_path}/fabfile.py configure_test_environment'.format(
            service_directory_path=self.__directory_path)
        )
