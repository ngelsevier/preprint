import os
from fabric.api import local, task
from fabric.context_managers import lcd
from getpass import getpass
from tempfile import mkstemp

from automation.database import migrate_database
from infrastructure.automation.build_functions import *
from infrastructure.automation.build_service_task import BuildServiceTask
from infrastructure.automation.follow_environment_logs_task import FollowEnvironmentLogsTask
from infrastructure.automation.restart_any_service_on_change_task import RestartAnyServiceOnChangeTask
from infrastructure.automation.report_environment_status_task import ReportEnvironmentStatusTask
from infrastructure.automation.start_service_environment_task import StartServiceEnvironmentTask
from infrastructure.automation.start_system_environment_task import StartSystemEnvironmentTask
from infrastructure.automation.stop_environment_task import StopEnvironmentTask
from infrastructure.automation.test_service_task import TestServiceTask
from infrastructure.automation.test_system_task import TestSystemTask


@task
def build(aws_account_id, aws_ecr_hostname, version, commit_hash, parent_image_version):
    build_heartbeat()

    build_author_replication_scheduler()

    build_author_events_replication_scheduler()

    with lcd(get_fabric_file_directory_path()):
        local('mkdir -p build/contract-tests')
        local('fab -f replicator/fabfile.py build')
        local('fab -f replicator/fabfile.py package:../build/contract-tests/old-platform-contract-tests.tgz')

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
def status(whole_system='n'):
    report_environment_status_task = ReportEnvironmentStatusTask(
        get_fabric_file_directory_path(), yes_indicated_by(whole_system)
    )

    report_environment_status_task.run()


@task()
def stop(whole_system='n'):
    stop_environment_task = StopEnvironmentTask(get_fabric_file_directory_path(), yes_indicated_by(whole_system))
    stop_environment_task.run()


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


@task
def build_author_replication_scheduler():
    with lcd(os.path.join(get_fabric_file_directory_path(), 'infrastructure/author-scheduler')):
        local('rm -rf build')
        local('mkdir build')

        with lcd('build'):
            local('cp ../job.py .')
            local('pip install requests -t .')
            local('find . -type f -name "*.pyc" -exec rm {} +')
            local('find . -exec touch --date="1970-01-01" {} +')
            local('zip -X -r ../author-scheduler.zip *')


@task
def build_author_events_replication_scheduler():
    with lcd(os.path.join(get_fabric_file_directory_path(), 'infrastructure/event-scheduler')):
        local('rm -rf build')
        local('mkdir build')

        with lcd('build'):
            local('cp ../job.py .')
            local('pip install requests -t .')
            local('find . -type f -name "*.pyc" -exec rm {} +')
            local('find . -exec touch --date="1970-01-01" {} +')
            local('zip -X -r ../event-scheduler.zip *')


@task
def build_heartbeat():
    with lcd(os.path.join(get_fabric_file_directory_path(), 'infrastructure/heartbeat')):
        local('rm -rf build')
        local('mkdir build')

        with lcd('build'):
            local('cp ../heartbeat.py .')
            local('pip install pg8000 -t .')
            local('find . -type f -name "*.pyc" -exec rm {} +')
            local('find . -exec touch --date="1970-01-01" {} +')
            local('zip -X -r ../heartbeat.zip *')


@task
def encrypt(aws_region, kms_master_key_arn):
    text = getpass('Enter text to encrypt: ')
    text2 = getpass('Enter text to encrypt again: ')

    if text != text2:
        raise Exception('You did not enter the same text again. Aborting...')

    try:
        _, unencrypted_text_file_path = mkstemp()

        with open(unencrypted_text_file_path, 'w') as unencrypted_text_file:
            unencrypted_text_file.write(text)

        print kms_encrypt_and_base64_encode_content_in(unencrypted_text_file_path, aws_region, kms_master_key_arn)
    finally:
        if unencrypted_text_file_path:
            os.remove(unencrypted_text_file_path)


@task
def configure_test_environment():
    fabfile_directory_path = get_fabric_file_directory_path()
    in_fabfile_directory(lambda: migrate_database(fabfile_directory_path, 'authors', dry_run=False))
    in_fabfile_directory(lambda: migrate_database(fabfile_directory_path, 'authors_integration_tests', dry_run=False))
