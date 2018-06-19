import json
from fabric.api import task

from infrastructure.automation.build_functions import *
from automation.database import migrate_database


@task
def label(aws_account_id, aws_ecr_hostname, label):
    service_directory_path = get_fabric_file_directory_path()

    label_service(
        label=label,
        configuration=create_configuration_with(
            aws_account_id,
            aws_ecr_hostname,
            '${VERSION}',
            service_directory_path
        )
    )


@task
def deploy(
        aws_account_id, aws_region, contact_details, environment, extended_configuration, global_remote_state_s3_bucket,
        global_remote_state_s3_key, global_remote_state_s3_region, dry_run='yes'):
    product = 'ssrn'
    extended_configuration_dictionary = json.loads(extended_configuration)

    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'service_deployer',
            }
        },
        'environment': environment,
        'product': product,
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn-{environment}'.format(environment=environment),
                'key': 'services/authors.tfstate'
            },
            'variables': {
                'aws_region': aws_region,
                'contact_details': contact_details,
                'encrypted_publisher_database_user_password': extended_configuration_dictionary[
                    'encryptedPublisherDatabaseUserPassword'
                ],
                'encrypted_heartbeat_database_user_password': extended_configuration_dictionary[
                    'encryptedHeartbeatDatabaseUserPassword'
                ],
                'encrypted_replicator_database_user_password': extended_configuration_dictionary[
                    'encryptedReplicatorDatabaseUserPassword'
                ],
                'old_platform_events_feed_base_url': extended_configuration_dictionary[
                    'oldPlatformEventsFeedBaseUrl'
                ],
                'old_platform_events_feed_http_basic_auth_username': extended_configuration_dictionary[
                    'oldPlatformEventsFeedBaseAuthUsername'
                ],
                'old_platform_events_feed_http_basic_auth_password': extended_configuration_dictionary[
                    'oldPlatformEventsFeedBaseAuthPassword'
                ],
                'events_feed_max_page_request_retries': extended_configuration_dictionary[
                    'eventsFeedMaxPageRequestRetries'
                ],
                'entity_feed_max_page_request_retries': extended_configuration_dictionary[
                    'entityFeedMaxPageRequestRetries'
                ],
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'product': product,
                'seconds_between_scheduling_replicator_jobs': extended_configuration_dictionary[
                    'secondsBetweenSchedulingReplicatorJobs'
                ],
                'replicator_entity_feed_job_batch_size': extended_configuration_dictionary[
                    'replicatorEntityFeedJobBatchSize'
                ],
                'replicator_entity_feed_job_database_upsert_batch_size': extended_configuration_dictionary[
                    'replicatorEntityFeedJobDatabaseUpsertBatchSize'
                ],
                'replicator_memory_reservation': extended_configuration_dictionary[
                    'replicatorMemoryReservation'
                ],
                'publisher_memory_reservation': extended_configuration_dictionary[
                    'publisherMemoryReservation'
                ],
                'publisher_max_concurrent_emissions': extended_configuration_dictionary[
                    'publisherMaxConcurrentEmissions'
                ]
            }
        }
    }
    do_dry_run = dry_run.lower() != 'no' and dry_run.lower() != 'n'

    aws_iam_role_session_credentials = deploy_to_aws_with_iam_role(configuration, do_dry_run)

    migrate_db = 'migrateDatabase' not in extended_configuration_dictionary \
                 or extended_configuration_dictionary['migrateDatabase']

    if migrate_db:
        migratedb(
            aws_region,
            aws_iam_role_session_credentials,
            extended_configuration_dictionary['encryptedMasterDatabaseUserPassword'],
            extended_configuration_dictionary['encryptedHeartbeatDatabaseUserPassword'],
            extended_configuration_dictionary['encryptedPublisherDatabaseUserPassword'],
            extended_configuration_dictionary['encryptedDatabaseMigrationUserPassword'],
            extended_configuration_dictionary['encryptedReplicatorDatabaseUserPassword'],
            dry_run
        )


@task
def migratedb(aws_region=None, aws_iam_role_session_credentials=None,
              encrypted_master_database_user_password=None, encrypted_heartbeat_database_user_password=None,
              encrypted_publisher_database_user_password=None, encrypted_database_migration_user_password=None,
              encrypted_replicator_database_user_password=None, dry_run='yes'):
    do_dry_run = dry_run.lower() != 'no' and dry_run.lower() != 'n'

    migrate_database(
        get_fabric_file_directory_path(),
        'authors',
        aws_region,
        encrypted_master_database_user_password,
        encrypted_heartbeat_database_user_password,
        encrypted_publisher_database_user_password,
        encrypted_database_migration_user_password,
        encrypted_replicator_database_user_password,
        aws_iam_role_session_credentials,
        do_dry_run
    )
