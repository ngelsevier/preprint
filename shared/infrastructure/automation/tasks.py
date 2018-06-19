from fabric.api import local
from fabric.context_managers import shell_env
import os


def apply_infrastructure_changes(configuration):
    __invoke_terraform('apply', configuration['terraform'])


def preview_infrastructure_changes(configuration):
    __invoke_terraform('plan', configuration['terraform'])


def preview_destroy_infrastructure(configuration):
    __invoke_terraform('plan -destroy', configuration['terraform'])


def destroy_infrastructure(configuration):
    __invoke_terraform('destroy', configuration['terraform'])


def __invoke_terraform(terraform_action, configuration):
    terraform_command = __terraform_command_for(
        terraform_action,
        configuration
    )

    __with_terraform_initialized(configuration['s3_remote_state'], lambda: local(terraform_command))


def __terraform_command_for(terraform_action, configuration):
    return ' '.join([
        ('terraform {}'.format(terraform_action)),
        '-input=false',
        __terraform_target_variables_command_line_string_from(configuration.get('targets', [])),
        __terraform_definition_variables_command_line_string_from(configuration.get('variables', {})),
    ])


def __terraform_definition_variables_command_line_string_from(variables):
    return ' '.join(
        map(lambda variable_name:
            "-var '{}={}'".format(
                escape_single_quotes_in_shell_string(variable_name),
                escape_single_quotes_in_shell_string(variables[variable_name])),
            variables))


def __terraform_target_variables_command_line_string_from(target_resources):
    return ' '.join(['-target={}'.format(resource) for resource in target_resources if len(resource) > 0])


def __with_terraform_initialized(s3_remote_state_configuration, do_work):
    local(
        "export TF_PLUGIN_CACHE_DIR={plugin_cache_directory_path}; "
        "terraform init "
        "-upgrade "
        "-input=false "
        "-get=true "
        "-backend-config='bucket={bucket}' "
        "-backend-config='key={key}' "
        "-backend-config='region={region}'".format(
            plugin_cache_directory_path=os.path.join(os.path.expanduser('~'), '.terraform.d/plugin-cache'),
            **s3_remote_state_configuration))

    do_work()


def escape_single_quotes_in_shell_string(string):
    return string.replace("'", "'\"'\"'")
