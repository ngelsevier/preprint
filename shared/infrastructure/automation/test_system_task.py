from fabric.context_managers import lcd
from fabric.operations import local

from infrastructure.automation.build_functions import TEMPORARY_ENVIRONMENT_DIRECTORY
from infrastructure.automation.system_sandbox_environment import SystemSandboxEnvironmentTask


class TestSystemTask(SystemSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, service_under_test_version, offline,
                 service_under_test_directory_path, simuated_environment, debug):
        super(TestSystemTask, self).__init__(
            aws_account_id, aws_ecr_hostname, offline, 'ssrn', TEMPORARY_ENVIRONMENT_DIRECTORY, False,
            service_under_test_directory_path, service_under_test_version, simuated_environment, False, debug
        )

        self.__service_under_test_directory_path = service_under_test_directory_path
        self.__offline = offline

    def _on_environment_started(self, environment, services):
        gradle_options = '--offline' if self.__offline else ''

        with lcd(self.__service_under_test_directory_path):
            local('gradle {gradle_options} '
                  '-P contractTestRealService=true '
                  ':test:support:fake-old-platform:functional-tests:check '
                  'interServiceContractTest '
                  ':test:test'.format(gradle_options=gradle_options))
