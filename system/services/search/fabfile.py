from fabric.api import task

from infrastructure.automation.build_functions import *
from infrastructure.automation.test_service_task import TestServiceTask
from infrastructure.automation.test_system_task import TestSystemTask
from infrastructure.automation.start_service_environment_task import StartServiceEnvironmentTask
from infrastructure.automation.start_system_environment_task import StartSystemEnvironmentTask
from infrastructure.automation.stop_environment_task import StopEnvironmentTask
from infrastructure.automation.report_environment_status_task import ReportEnvironmentStatusTask
from infrastructure.automation.follow_environment_logs_task import FollowEnvironmentLogsTask
from infrastructure.automation.restart_any_service_on_change_task import RestartAnyServiceOnChangeTask
from infrastructure.automation.build_service_task import BuildServiceTask


@task
def build(aws_account_id, aws_ecr_hostname, version, commit_hash, parent_image_version):
    build_service_task = BuildServiceTask(
        aws_account_id,
        aws_ecr_hostname,
        version,
        commit_hash,
        parent_image_version,
        get_fabric_file_directory_path()
    )

    build_service_task.run()


@task
def test(aws_account_id, aws_ecr_hostname, version='local', offline='n'):
    test_service_task = TestServiceTask(
        aws_account_id,
        aws_ecr_hostname,
        version,
        yes_indicated_by(offline),
        get_fabric_file_directory_path(),
        debug=False
    )

    test_service_task.run()


@task
def system_test(aws_account_id, aws_ecr_hostname, version='local', simulated_environment='production', offline='n'):
    test_system_task = TestSystemTask(
        aws_account_id,
        aws_ecr_hostname,
        version,
        yes_indicated_by(offline),
        get_fabric_file_directory_path(),
        simulated_environment,
        debug=False
    )

    test_system_task.run()


@task
def configure_test_environment():
    pass


@task
def go(aws_account_id, aws_ecr_hostname, whole_system='n', simulated_environment='production', offline='n',
       log_aggregation='n', debug='n'):
    system_under_test_service_version = 'local'
    start_whole_system = yes_indicated_by(whole_system)
    run_offline = yes_indicated_by(offline)
    run_with_debug = yes_indicated_by(debug)
    service_directory_path = get_fabric_file_directory_path()

    if start_whole_system:
        start_environment_task = StartSystemEnvironmentTask(
            aws_account_id,
            aws_ecr_hostname,
            system_under_test_service_version,
            run_offline,
            service_directory_path,
            simulated_environment,
            yes_indicated_by(log_aggregation),
            debug=run_with_debug
        )
    else:
        start_environment_task = StartServiceEnvironmentTask(
            aws_account_id,
            aws_ecr_hostname,
            system_under_test_service_version,
            run_offline,
            service_directory_path,
            debug=run_with_debug
        )

    start_environment_task.run()


@task()
def stop(whole_system='n'):
    stop_environment_task = StopEnvironmentTask(get_fabric_file_directory_path(), yes_indicated_by(whole_system))
    stop_environment_task.run()


@task()
def status(whole_system='n'):
    report_environment_status_task = ReportEnvironmentStatusTask(
        get_fabric_file_directory_path(), yes_indicated_by(whole_system)
    )

    report_environment_status_task.run()


@task()
def logs(whole_system='n'):
    follow_environment_logs_task = FollowEnvironmentLogsTask(
        get_fabric_file_directory_path(), yes_indicated_by(whole_system)
    )

    follow_environment_logs_task.run()


@task()
def watch(whole_system='n'):
    restart_any_service_on_change_task = RestartAnyServiceOnChangeTask(
        get_fabric_file_directory_path(), yes_indicated_by(whole_system)
    )

    restart_any_service_on_change_task.run()
