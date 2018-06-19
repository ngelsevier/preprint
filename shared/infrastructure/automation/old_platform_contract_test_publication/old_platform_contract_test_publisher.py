from s3_bucket import S3Bucket
from fabric.api import local
from fabric.context_managers import lcd


class OldPlatformContractTestPublisher:
    ARTIFACTS_S3_KEY = 'artifacts'
    SOURCE_CONTRACT_TESTS_JAR_FILE = 'old-platform-contract-tests.jar'
    ALL_COMPONENTS = [
        {'service': 'authors', 'component': 'replicator'},
        {'service': 'papers', 'component': 'replicator'},
        {'service': 'frontend', 'component': 'website'}
    ]

    def __init__(self, aws_region, aws_account_id, environment):
        self.__s3_bucket = S3Bucket(
            aws_region=aws_region,
            aws_account_id=aws_account_id,
            environment=environment,
            bucket_name=('elsevier-ssrn-{environment}'.format(environment=environment))
        )

    def publish_for(self, updated_service, updated_component):
        self.__publish_contract_tests_for(updated_service, updated_component)
        self.__publish_contract_tests_bundle_with(updated_service, updated_component)

    def __publish_contract_tests_for(self, service, component):
        self.__s3_bucket.publish_as_public_s3_object(
            source_file=OldPlatformContractTestPublisher.SOURCE_CONTRACT_TESTS_JAR_FILE,
            destination_key=self.__contract_test_jar_s3_key(service, component)
        )

    def __publish_contract_tests_bundle_with(self, updated_service, updated_component):
        local('rm -rf bundle')
        local('mkdir bundle')

        with lcd('bundle'):
            local('cp ../{source_file} {destination_file}'.format(
                source_file=OldPlatformContractTestPublisher.SOURCE_CONTRACT_TESTS_JAR_FILE,
                destination_file=self.__bundled_contract_test_jar_name(updated_service, updated_component)
            ))

            for other_service in OldPlatformContractTestPublisher.ALL_COMPONENTS:
                service = other_service['service']
                component = other_service['component']

                if not (service == updated_service and component == updated_component):
                    artifact_key = self.__contract_test_jar_s3_key(service, component)

                    local('curl -fLs -o {output_file} {artifact_url}'.format(
                        output_file=self.__bundled_contract_test_jar_name(service, component),
                        artifact_url=(self.__s3_bucket.public_url_for(artifact_key))
                    ))

            local('tar -cf contract-tests.tar *.jar')

            self.__s3_bucket.publish_as_public_s3_object(
                source_file='contract-tests.tar',
                destination_key='{artifacts_s3_prefix}/old-platform-contract-tests.tar'.format(
                    artifacts_s3_prefix=OldPlatformContractTestPublisher.ARTIFACTS_S3_KEY
                )
            )

    @staticmethod
    def __contract_test_jar_s3_key(service, component):
        return '{artifacts_s3_prefix}/{service}/{component}-old-platform-contract-tests.jar'.format(
            artifacts_s3_prefix=OldPlatformContractTestPublisher.ARTIFACTS_S3_KEY, service=service, component=component
        )

    @staticmethod
    def __bundled_contract_test_jar_name(service, component):
        return '{service}-{component}-contract-tests.jar'.format(service=service, component=component)
