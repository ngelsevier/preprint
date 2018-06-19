import string


class ServiceConfiguration:
    def __init__(self, service_configuration):
        self.service_configuration = service_configuration

    def get_mounted_host_file_paths(self):
        volumes = self.service_configuration.get('volumes')
        return self.__extract_host_file_paths_from(volumes) if volumes else []

    @staticmethod
    def __extract_host_file_paths_from(volumes):
        def is_mounted_volume(volume_configuration_string):
            return ':' in volume_configuration_string

        def host_file_path(volume_configuration_string):
            return string.split(volume_configuration_string, ':')[0]

        return map(host_file_path, filter(is_mounted_volume, volumes))
