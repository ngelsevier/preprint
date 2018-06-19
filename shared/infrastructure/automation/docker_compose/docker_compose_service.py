class DockerComposeService:
    def __init__(self, name, mounted_host_file_paths, service_controller):
        self.name = name
        self.__mounted_host_file_paths = mounted_host_file_paths
        self.__service_controller = service_controller

    def restart_when_mounted_host_file_changes_detected_by(self, file_watcher):
        if self.__mounted_host_file_paths:
            file_watcher.on_change_of_file_under(
                self.__mounted_host_file_paths,
                lambda: self.__service_controller.restart(self),
                '[{service_name}] '.format(service_name=self.name)
            )
