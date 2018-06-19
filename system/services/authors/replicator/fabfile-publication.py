from fabric.api import local, task

from infrastructure.automation.functions import *
from infrastructure.automation.old_platform_contract_test_publication.old_platform_contract_test_publisher import \
    OldPlatformContractTestPublisher


@task
def publish(aws_region, aws_account_id, environment):
    old_platform_contract_test_publisher = OldPlatformContractTestPublisher(aws_region, aws_account_id, environment)

    in_fabfile_directory(lambda: old_platform_contract_test_publisher.publish_for('authors', 'replicator'))


@task
def run():
    in_fabfile_directory(lambda: local('java -jar old-platform-contract-tests.jar --test-report report.xml'))
