import os


class ProjectLayout(object):
    def __init__(self, service_directory_path):
        self.__service_directory_path = service_directory_path

    def system_wide_log_aggregation_docker_compose_file_path(self):
        system_directory_path = self.system_directory_path()
        return os.path.join(system_directory_path, 'docker-compose-log-aggregation.yml')

    def system_directory_path(self):
        return os.path.join(self.__service_directory_path, '../..')
