from fabric.context_managers import hide, settings
from fabric.api import local
from datetime import datetime


class ServiceController:
    def __init__(self, suppressed_fabric_log_levels, docker_compose_options):
        self.docker_compose_options = docker_compose_options
        self.suppressed_fabric_log_levels = suppressed_fabric_log_levels

    def restart(self, service):
        with settings(hide(*self.suppressed_fabric_log_levels), warn_only=True):
            print '{timestamp}: Restarting {docker_compose_service_name}...'.format(
                timestamp=datetime.now(), docker_compose_service_name=service.name)
            local('docker-compose {options} restart {docker_compose_service_name}'.format(
                docker_compose_service_name=service.name, options=self.docker_compose_options))
