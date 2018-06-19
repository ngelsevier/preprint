from fabric.context_managers import settings, hide
import os

import time
import yaml

from docker_compose_service import DockerComposeService
from service_configuration import ServiceConfiguration
from service_controller import ServiceController
from ..functions import *


class DockerComposeEnvironment(object):
    def __init__(self, directory_path, offline, services, temporary_environment_directory_path,
                 docker_image_registry, additional_docker_compose_file_paths=[],
                 additional_docker_compose_environment_variables={}, debug=False):
        self.__directory_path = directory_path
        self.__debug = debug
        self.__docker_compose_configuration = None
        self.__offline = offline
        self.__services = services
        self.__temporary_environment_directory_path = temporary_environment_directory_path
        self.__docker_image_registry = docker_image_registry
        self.__additional_docker_compose_environment_variables = additional_docker_compose_environment_variables

        default_docker_compose_file_path = os.path.join(self.__directory_path, 'docker-compose.yml')
        docker_compose_file_paths = [default_docker_compose_file_path] + additional_docker_compose_file_paths

        self.__additional_docker_compose_options = ' '.join(
            map(lambda file_path: '-f {}'.format(file_path), docker_compose_file_paths)
        )

    def start(self):
        if not self.__offline:
            self.__docker_image_registry.login()

        def start_docker_compose_configuration():
            local(
                'docker-compose {additional_options} down --remove-orphans --volumes'.format(
                    additional_options=self.__additional_docker_compose_options))
            local('docker kill $(docker container ls --all --quiet) >/dev/null 2>&1 || /bin/true')

            if self.__offline:
                local('docker-compose {additional_options} build'.format(
                    additional_options=self.__additional_docker_compose_options))
            else:
                local('docker-compose {additional_options} pull'.format(
                    additional_options=self.__additional_docker_compose_options))
                local('docker-compose {additional_options} build --pull'.format(
                    additional_options=self.__additional_docker_compose_options))

            local('docker-compose {additional_options} up -d'.format(
                additional_options=self.__additional_docker_compose_options))

        self.__with_docker_compose_environment_configured(start_docker_compose_configuration)

        for service in self.__services:
            service.configure_test_environment()

    def stop(self):
        local('docker-compose {additional_options} stop'.format(
            additional_options=self.__additional_docker_compose_options))

    def follow_logs(self):
        def at_least_one_service_is_running():
            with settings(hide('running', 'warnings', 'output'), warn_only=True):
                return local(
                    'docker-compose ps {additional_options} -q | '
                    'xargs docker inspect | '
                    'jq -e \'.[] |= .State.Status != "exited" | any\''.format(
                        additional_options=self.__additional_docker_compose_options)
                ).succeeded

        while True:
            if at_least_one_service_is_running():
                with settings(warn_only=True):
                    local('docker-compose {additional_options} logs --follow --tail=0 --timestamps'.format(
                        additional_options=self.__additional_docker_compose_options
                    ))
            time.sleep(0.1)

    def status(self):
        local('docker-compose {additional_options} ps'.format(
            additional_options=self.__additional_docker_compose_options))

    def destroy(self):
        with lcd(self.__directory_path):
            local('docker-compose {additional_options} down --remove-orphans --volumes'.format(
                additional_options=self.__additional_docker_compose_options
            ))

    def restart_services_when_mounted_host_file_changes_detected_by(self, file_watcher):
        for service in self.__docker_compose_services():
            service.restart_when_mounted_host_file_changes_detected_by(file_watcher)

    def has_service_named(self, service_name):
        return any(service.name == service_name for service in self.__docker_compose_services())

    def __docker_compose_services(self):
        docker_compose_configuration = self.__get_extended_docker_compose_configuration()

        service_controller = ServiceController([] if self.__debug else ['running', 'warnings', 'output'],
                                               self.__additional_docker_compose_options)

        return reduce(
            lambda services, service_key_value_pair: services + [
                DockerComposeEnvironment.__create_service(service_key_value_pair, service_controller)
            ],
            docker_compose_configuration['services'].iteritems(),
            []
        )

    def __get_extended_docker_compose_configuration(self):
        if not self.__docker_compose_configuration:
            self.__docker_compose_configuration = self.__load_docker_compose_configuration()

        return self.__docker_compose_configuration

    @staticmethod
    def __create_service(service_name_and_configuration, service_controller):
        (service_name, service_configuration) = service_name_and_configuration
        configuration = ServiceConfiguration(service_configuration)

        return DockerComposeService(
            service_name,
            configuration.get_mounted_host_file_paths(),
            service_controller
        )

    def __load_docker_compose_configuration(self):
        docker_compose_configuration_yaml = self.__with_docker_compose_environment_configured(
            lambda: local('docker-compose {additional_options} config'.format(
                additional_options=self.__additional_docker_compose_options), capture=True)
        )
        return yaml.load(docker_compose_configuration_yaml)

    def __with_docker_compose_environment_configured(self, do_work):
        with shell_env(**(self.__get_docker_compose_environment_variables())):
            return do_work()

    def __get_docker_compose_environment_variables(self):
        def collect_docker_compose_environment_variables_for_service(existing_environment_variables,
                                                                     service):
            def collect_docker_compose_environment_variables_for_component(accumulated_environment_variables,
                                                                           component):
                environment_variable_name = '{service}_{component}_IMAGE'.format(
                    service=service.name().upper(),
                    component=component.upper().replace('-', '_')
                )

                docker_image_tag = self.__docker_image_registry.image_tag_for(service.name(), component,
                                                                              service.version())

                return dict(accumulated_environment_variables, **{environment_variable_name: docker_image_tag})

            service_uppercase_name = service.name().upper()

            sandbox_directory_path_key = '{service_name}_SANDBOX_DIRECTORY_PATH'.format(
                service_name=service_uppercase_name)
            sandbox_directory_path_value = '.' if service.is_local_version() else self.__temporary_environment_directory_path

            docker_compose_service_mode_key = '{service}_DOCKER_COMPOSE_SERVICE_MODE'.format(
                service=service_uppercase_name)

            new_environment_variables = {
                sandbox_directory_path_key: sandbox_directory_path_value,
                docker_compose_service_mode_key: 'build' if service.is_local_version() else 'image'
            }

            updated_environment_variables = dict(existing_environment_variables, **new_environment_variables)

            if service.is_local_version():
                return updated_environment_variables
            else:
                return reduce(
                    collect_docker_compose_environment_variables_for_component,
                    service.components(),
                    updated_environment_variables
                )

        return reduce(collect_docker_compose_environment_variables_for_service, self.__services,
                      self.__additional_docker_compose_environment_variables)
