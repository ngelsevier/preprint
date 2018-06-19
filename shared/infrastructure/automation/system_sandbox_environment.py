import os
import yaml
from infrastructure.automation.start_up_sandbox_environment_task import StartUpSandboxEnvironmentTask
from project_layout import ProjectLayout


class SystemSandboxEnvironmentTask(StartUpSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, offline, product, temporary_environment_directory_path,
                 leave_environment_running, service_under_test_directory_path, service_under_test_version,
                 simulated_environment, log_aggregation, debug):
        project_layout = ProjectLayout(service_under_test_directory_path)
        system_directory_path = project_layout.system_directory_path()

        system_configuration = self.__load_system_configuration(system_directory_path)

        service_configurations = self.__load_service_configurations(
            system_configuration, system_directory_path, service_under_test_version, service_under_test_directory_path,
            simulated_environment, temporary_environment_directory_path
        )

        additional_docker_compose_file_paths = [
            project_layout.system_wide_log_aggregation_docker_compose_file_path()] if log_aggregation else []

        super(SystemSandboxEnvironmentTask, self).__init__(
            aws_account_id, aws_ecr_hostname, system_directory_path, offline, product,
            temporary_environment_directory_path, True, leave_environment_running,
            service_configurations,
            system_configuration['fake-services'],
            additional_docker_compose_file_paths,
            {'SHIP_LOGS': str(log_aggregation).lower()},
            debug
        )

    @staticmethod
    def __load_system_configuration(system_directory_path):
        with open(os.path.join(system_directory_path, 'system.yml'), 'r') as stream:
            system_configuration = yaml.load(stream)
        return system_configuration

    @staticmethod
    def __load_service_configurations(system_configuration, system_directory_path, service_under_test_version,
                                      service_under_test_directory_path, simulated_environment,
                                      temporary_environment_directory_path):
        def collect_service_configuration(accumulated_services, service_name):
            directory_path = os.path.join(system_directory_path, 'services', service_name)
            with open(os.path.join(directory_path, 'service.yml'), 'r') as stream:
                service_configuration = yaml.load(stream)
                service_configuration['directory_path'] = directory_path
                service_is_under_test = os.path.realpath(directory_path) == os.path.realpath(
                    service_under_test_directory_path)
                service_version = (service_under_test_version if service_is_under_test else simulated_environment)

                return dict(
                    accumulated_services,
                    **{
                        service_name: {
                            'name': service_configuration['name'],
                            'components': service_configuration['components'],
                            'version': service_version,
                            'testing': service_is_under_test,
                            'sandbox_directory_path': './' if service_version == 'local'
                            else temporary_environment_directory_path,
                            'directory_path': service_configuration['directory_path']
                        }
                    }
                )

        return reduce(collect_service_configuration, system_configuration['services'], {})
