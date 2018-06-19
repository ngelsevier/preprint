import os
import threading
from fabric.context_managers import lcd, settings, hide
from fabric.operations import local

import signal
import sys
import time

from docker_compose.docker_compose_environment import DockerComposeEnvironment
from docker_utils import DockerUtils
from gradle_docker_context_generator import GradleDockerContextGenerator
from watch.inotifywait_file_watcher import InotifywaitFileWatcher

PROCESS_INTERRUPTED_EVENT = threading.Event()


def __signal_handler(signal, frame):
    PROCESS_INTERRUPTED_EVENT.set()
    sys.exit(0)


signal.signal(signal.SIGINT, __signal_handler)


class TestEnvironment(object):
    def __init__(self, directory_path, whole_system, services, offline,
                 temporary_environment_directory_path, docker_image_registry,
                 fake_services, aws_ecr_hostname, product, additional_docker_compose_file_paths=[],
                 additional_docker_compose_environment_variables={}, debug=False):
        self.__directory_path = directory_path
        self.__whole_system = whole_system
        self.__services = services
        self.__offline = offline
        self.__temporary_environment_directory_path = temporary_environment_directory_path
        self.__debug = debug
        self.__docker_image_registry = docker_image_registry
        self.__docker_compose_environment = DockerComposeEnvironment(
            directory_path,
            offline,
            services,
            temporary_environment_directory_path,
            docker_image_registry,
            additional_docker_compose_file_paths,
            additional_docker_compose_environment_variables,
            debug
        )
        self.__fake_services = fake_services
        self.__aws_ecr_hostname = aws_ecr_hostname
        self.__product = product

    def start(self):
        if self.__whole_system:
            self.__ensure_sandbox_environment_files_exist()

        for service in self.__services:
            if service.is_local_version():
                GradleDockerContextGenerator.generate_docker_contexts_for(
                    components=service.components(),
                    service_directory_path=service.directory_path(),
                    offline=self.__offline
                )

        GradleDockerContextGenerator.generate_docker_contexts_for(
            components=self.__fake_services,
            service_directory_path=self.__directory_path,
            offline=self.__offline
        )

        self.__docker_compose_environment.start()

    def restart_local_components_on_change(self):
        file_watcher = InotifywaitFileWatcher(PROCESS_INTERRUPTED_EVENT, self.__debug)
        self.__docker_compose_environment.restart_services_when_mounted_host_file_changes_detected_by(file_watcher)
        file_watcher.wait_whilst_watching()

    def follow_logs(self):
        self.__docker_compose_environment.follow_logs()

    def stop(self):
        self.__docker_compose_environment.stop()

    def status(self):
        self.__docker_compose_environment.status()

    def destroy(self):
        self.__docker_compose_environment.destroy()

    def has_service_named(self, service_name):
        return self.__docker_compose_environment.has_service_named(service_name)

    def __ensure_sandbox_environment_files_exist(self):
        sandbox_directory_path = self.__temporary_environment_directory_path
        scratch_repo_directory_path = os.path.join(sandbox_directory_path, 'scratch')
        sandbox_services_directory_path = os.path.join(sandbox_directory_path, 'services')

        local('rm -rf {sandbox_directory_path}'.format(sandbox_directory_path=sandbox_directory_path))
        local('mkdir -p {directory_path}'.format(directory_path=sandbox_directory_path))
        local('mkdir -p {directory_path}'.format(directory_path=sandbox_services_directory_path))
        local('git clone --single-branch --no-checkout --quiet '
              '{repository_root_directory_path} {scratch_repo_directory_path}'
              .format(repository_root_directory_path=os.path.join(self.__directory_path, '..'),
                      scratch_repo_directory_path=scratch_repo_directory_path))

        if not self.__offline:
            with lcd(scratch_repo_directory_path):
                local('git fetch')

            self.__docker_image_registry.login()

        for service in self.__services:
            if not service.is_local_version():
                docker_image_tag = DockerUtils.get_service_component_image_tag(
                    self.__aws_ecr_hostname,
                    self.__product,
                    service.name(),
                    service.components()[0],
                    service.version()
                )

                if not self.__offline:
                    local('docker pull {image_tag}'.format(image_tag=docker_image_tag))

                docker_commit_label_name = 'com.ssrn.commit'

                commit_hash = local(
                    'docker inspect '
                    '--format="{{{{ index .ContainerConfig.Labels \\"{docker_commit_label_name}\\" }}}}" '
                    '{docker_image_tag}'.format(docker_image_tag=docker_image_tag,
                                                docker_commit_label_name=docker_commit_label_name),
                    capture=True
                ).stdout

                if not commit_hash:
                    raise Exception(
                        'Docker image {docker_image_tag} did not have a label named '
                        '"{docker_commit_label_name}"'.format(
                            docker_image_tag=docker_image_tag,
                            docker_commit_label_name=docker_commit_label_name
                        ))

                with lcd(scratch_repo_directory_path):
                    local('git checkout --quiet {commit_hash}'.format(commit_hash=commit_hash))

                local(
                    'cp --dereference --recursive '
                    '{source_service_directory_path} '
                    '{destination_service_directory_path}'.format(
                        source_service_directory_path=os.path.join(scratch_repo_directory_path, 'system', 'services',
                                                                   service.name()),
                        destination_service_directory_path=os.path.join(sandbox_services_directory_path, service.name())
                    ))
