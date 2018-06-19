from infrastructure.automation.build_functions import TEMPORARY_ENVIRONMENT_DIRECTORY
from infrastructure.automation.service_sandbox_environment import ServiceSandboxEnvironmentTask


class StartServiceEnvironmentTask(ServiceSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, version, offline, service_directory_path, debug):
        super(StartServiceEnvironmentTask, self).__init__(
            aws_account_id, aws_ecr_hostname, offline, 'ssrn', TEMPORARY_ENVIRONMENT_DIRECTORY, True,
            service_directory_path, version, debug
        )

    def _on_environment_started(self, environment, services):
        environment.restart_local_components_on_change()
