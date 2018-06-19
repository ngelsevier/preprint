from infrastructure.automation.sandbox_environment_task import SandboxEnvironmentTask


class StartUpSandboxEnvironmentTask(SandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, environment_directory_path, offline,
                 product, temporary_environment_directory_path, whole_system, leave_environment_running,
                 service_configurations, fake_services, additional_docker_compose_file_paths=[],
                 additional_docker_compose_environment_variables={}, debug=False):
        super(StartUpSandboxEnvironmentTask, self).__init__(
            environment_directory_path, offline, product, whole_system, aws_account_id, aws_ecr_hostname,
            temporary_environment_directory_path, service_configurations, fake_services,
            additional_docker_compose_file_paths, additional_docker_compose_environment_variables, debug
        )

        self.__leave_environment_running = leave_environment_running

    def _run(self, environment, services):
        self._before_starting_environment(services)

        try:
            environment.start()
            self._on_environment_started(environment, services)

        finally:
            if not self.__leave_environment_running:
                environment.stop()

        if not self.__leave_environment_running:
            environment.destroy()

    def _before_starting_environment(self, services):
        pass

    def _on_environment_started(self, environment, services):
        pass
