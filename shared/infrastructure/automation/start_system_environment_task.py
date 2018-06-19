from infrastructure.automation.build_functions import TEMPORARY_ENVIRONMENT_DIRECTORY
from infrastructure.automation.system_sandbox_environment import SystemSandboxEnvironmentTask


class StartSystemEnvironmentTask(SystemSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, service_under_test_version, offline,
                 service_under_test_directory_path, simulated_environment, log_aggregation, debug):
        super(StartSystemEnvironmentTask, self).__init__(
            aws_account_id, aws_ecr_hostname, offline, 'ssrn', TEMPORARY_ENVIRONMENT_DIRECTORY, True,
            service_under_test_directory_path, service_under_test_version, simulated_environment, log_aggregation,
            debug
        )

    def _on_environment_started(self, environment, services):
        environment.restart_local_components_on_change()
