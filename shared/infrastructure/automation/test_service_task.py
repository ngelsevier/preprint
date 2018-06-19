from fabric.context_managers import lcd
from fabric.operations import local

from infrastructure.automation.build_functions import TEMPORARY_ENVIRONMENT_DIRECTORY
from infrastructure.automation.service_sandbox_environment import ServiceSandboxEnvironmentTask


class TestServiceTask(ServiceSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, version, offline, service_directory_path, debug):
        super(TestServiceTask, self).__init__(
            aws_account_id, aws_ecr_hostname, offline, 'ssrn', TEMPORARY_ENVIRONMENT_DIRECTORY, False,
            service_directory_path, version, debug
        )

        self.__offline = offline

    def _before_starting_service_sandbox_environment(self, service):
        gradle_options = '--offline' if self.__offline else ''

        if service.is_local_version():
            with lcd(service.directory_path()):
                local('gradle {gradle_options} check'.format(gradle_options=gradle_options))

    def _on_service_sandbox_environment_started(self, environment, service):
        gradle_options = '--offline' if self.__offline else ''

        additional_gradle_tasks = ':test:support:fake-old-platform:functional-tests:check' \
            if environment.has_service_named('fake-old-platform') else ''

        with lcd(service.directory_path()):
            local('gradle {gradle_options} {additional_gradle_tasks} environmentTest'.format(
                gradle_options=gradle_options,
                additional_gradle_tasks=additional_gradle_tasks
            ))
