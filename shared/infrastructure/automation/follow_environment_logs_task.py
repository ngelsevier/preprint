from sandbox_environment_task import SandboxEnvironmentTask

from project_layout import ProjectLayout


class FollowEnvironmentLogsTask(SandboxEnvironmentTask):
    def __init__(self, service_under_test_directory_path, whole_system):
        project_layout = ProjectLayout(service_under_test_directory_path)

        super(FollowEnvironmentLogsTask, self).__init__(
            project_layout.system_directory_path() if whole_system
            else service_under_test_directory_path,
            True,
            'ssrn',
            whole_system,
            additional_docker_compose_file_paths=[
                project_layout.system_wide_log_aggregation_docker_compose_file_path()]
            if whole_system else []
        )

    def _run(self, environment, services):
        environment.follow_logs()


