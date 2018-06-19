import os
import yaml

from infrastructure.automation.start_up_sandbox_environment_task import StartUpSandboxEnvironmentTask


class ServiceSandboxEnvironmentTask(StartUpSandboxEnvironmentTask):
    def __init__(self, aws_account_id, aws_ecr_hostname, offline, product, temporary_environment_directory_path,
                 leave_environment_running, service_directory_path, version, debug):
        service_configuration = self.__load_service_configuration(service_directory_path, version)

        super(ServiceSandboxEnvironmentTask, self).__init__(
            aws_account_id,
            aws_ecr_hostname,
            service_configuration['directory_path'],
            offline,
            product,
            temporary_environment_directory_path,
            False,
            leave_environment_running,
            {service_configuration['name']: service_configuration},
            service_configuration['fake-services'],
            [],
            {},
            debug
        )

    def _before_starting_environment(self, services):
        self._before_starting_service_sandbox_environment(services[0])

    def _before_starting_service_sandbox_environment(self, service):
        pass

    def _on_environment_started(self, environment, services):
        self._on_service_sandbox_environment_started(environment, services[0])

    def _on_service_sandbox_environment_started(self, environment, service):
        pass

    @staticmethod
    def __load_service_configuration(service_directory_path, version):
        with open(os.path.join(service_directory_path, 'service.yml'), 'r') as stream:
            loaded_service_configuration = yaml.load(stream)

        return dict({'version': version, 'directory_path': service_directory_path}, **loaded_service_configuration)
