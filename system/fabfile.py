from automation.shared.functions import *

AWS_ECR_HOSTNAME = '756659522569.dkr.ecr.us-east-1.amazonaws.com'
AWS_ACCOUNT_ID = '756659522569'


@task
def login_to_ecr():
    log_docker_in_to_aws_ecr(
        {
            'ecr': {
                'region': 'us-east-1',
                'hostname': AWS_ECR_HOSTNAME
            },
            'iam_roles': {
                'ecr_image_consumer': {
                    'account_id': AWS_ACCOUNT_ID,
                    'short_name': 'ecr_image_consumer'
                }
            }
        },
        'ssrn',
        'ecr_image_consumer'
    )


@task
def build(service=None, alarm='n', offline='n', test_service='y', test_system='y'):
    testing_service = yes_indicated_by(test_service)
    testing_system = yes_indicated_by(test_system)

    if service:
        __run_tests_for_service(service, AWS_ACCOUNT_ID, AWS_ECR_HOSTNAME, offline, alarm, testing_service,
                                testing_system)
        return

    for service in ['frontend', 'search', 'papers', 'authors', 'logging']:
        __run_tests_for_service(service, AWS_ACCOUNT_ID, AWS_ECR_HOSTNAME, offline, alarm, testing_service,
                                testing_system)


@task
def go(service, whole_system='n', simulated_environment='production', offline='n', log_aggregation='n', debug='n'):
    local('fab -f {service_directory_path}/fabfile.py go:'
          'aws_account_id={aws_account_id},'
          'aws_ecr_hostname={aws_ecr_hostname},'
          'whole_system={whole_system},'
          'simulated_environment={simulated_environment},'
          'offline={offline},'
          'log_aggregation={log_aggregation},'
          'debug={debug}'.format(service_directory_path=__directory_path_for(service),
                                 aws_account_id=AWS_ACCOUNT_ID,
                                 aws_ecr_hostname=AWS_ECR_HOSTNAME,
                                 whole_system=whole_system,
                                 simulated_environment=simulated_environment,
                                 offline=offline,
                                 log_aggregation=log_aggregation,
                                 debug=debug))


@task
def stop(service, whole_system='n'):
    local(
        'fab -f {service_directory_path}/fabfile.py stop:whole_system={whole_system}'.format(
            service_directory_path=__directory_path_for(service), whole_system=whole_system)
    )


@task
def logs(service, whole_system='n'):
    local(
        'fab -f {service_directory_path}/fabfile.py logs:whole_system={whole_system}'.format(
            service_directory_path=__directory_path_for(service), whole_system=whole_system)
    )


@task
def status(service, whole_system='n'):
    local(
        'fab -f {service_directory_path}/fabfile.py status:whole_system={whole_system}'.format(
            service_directory_path=__directory_path_for(service), whole_system=whole_system)
    )


@task
def watch(service, whole_system='n'):
    local(
        'fab -f {service_directory_path}/fabfile.py watch:whole_system={whole_system}'.format(
            service_directory_path=__directory_path_for(service), whole_system=whole_system)
    )


def __run_tests_for_service(service, aws_account_id, aws_ecr_hostname, offline, alarm, test_service, test_system):
    service_directory_path = __directory_path_for(service)

    if test_service:
        local('fab -f {service_directory_path}/fabfile.py test:'
              'aws_account_id={aws_account_id},'
              'aws_ecr_hostname={aws_ecr_hostname},'
              'version=local,'
              'offline={offline}'.format(
            service_directory_path=service_directory_path,
            aws_account_id=aws_account_id,
            aws_ecr_hostname=aws_ecr_hostname,
            offline=offline)
        )

    if test_system:
        local('fab -f {service_directory_path}/fabfile.py system_test:'
              'aws_account_id={aws_account_id},'
              'aws_ecr_hostname={aws_ecr_hostname},'
              'version=local,'
              'simulated_environment={simulated_environment},'
              'offline={offline}'.format(
            service_directory_path=service_directory_path,
            aws_account_id=aws_account_id,
            aws_ecr_hostname=aws_ecr_hostname,
            simulated_environment='development' if yes_indicated_by(alarm) else 'production',
            offline=offline)
        )


def __directory_path_for(service):
    fabfile_directory_path = get_fabric_file_directory_path()
    return '{fabfile_directory_path}/services/{service}'.format(
        service=service,
        fabfile_directory_path=fabfile_directory_path
    ) if service else fabfile_directory_path
