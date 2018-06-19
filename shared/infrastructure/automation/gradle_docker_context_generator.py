from fabric.context_managers import settings, hide
from fabric.operations import local


class GradleDockerContextGenerator(object):
    @staticmethod
    def generate_docker_contexts_for(components, service_directory_path, offline=False):
        gradle_project_paths = GradleDockerContextGenerator.__gradle_project_paths_from_directory_paths(components)
        gradle_project_paths = [
            gradle_project_path for gradle_project_path in gradle_project_paths if
            GradleDockerContextGenerator.__is_known_to_gradle(gradle_project_path, service_directory_path, offline)
        ]

        gradle_options = '--offline' if offline else ''

        if gradle_project_paths:
            local('gradle -p {gradle_project_directory_path} {gradle_options} {classpath_tasks}'.format(
                classpath_tasks=GradleDockerContextGenerator.__map_join_format('{item}:classpath', gradle_project_paths, ' '),
                gradle_project_directory_path=service_directory_path,
                gradle_options=gradle_options
            ))

    @staticmethod
    def __gradle_project_paths_from_directory_paths(project_directory_paths):
        return map(lambda component: component.replace('/', ':'), project_directory_paths)

    @staticmethod
    def __is_known_to_gradle(gradle_project_path, gradle_build_file_directory_path, offline):
        gradle_options = '--offline' if offline else ''

        with settings(hide('running', 'warnings', 'output'), warn_only=True):
            return local(
                'gradle -p {gradle_build_file_directory_path} {gradle_options} '
                '-PprojectGradlePath="{gradle_project_path}" '
                'projectExists'.format(gradle_project_path=gradle_project_path,
                                       gradle_options=gradle_options,
                                       gradle_build_file_directory_path=gradle_build_file_directory_path)
            ).succeeded

    @staticmethod
    def __map_join_format(template, items, delimiter, prefix_delimiter=False):
        joined_string = delimiter.join(map(lambda item: template.format(item=item), items))
        return delimiter + joined_string if prefix_delimiter else joined_string