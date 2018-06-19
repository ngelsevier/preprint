from fabric.api import local
from fabric.context_managers import settings, hide
from infrastructure.automation.functions import using_kms_decrypt_or_default
from datetime import datetime, timedelta
import os
import re
import time


def migrate_database(service_directory_path, database_name, aws_region=None,
                     encrypted_master_database_user_password=None, encrypted_heartbeat_database_user_password=None,
                     encrypted_publisher_database_user_password=None,
                     encrypted_database_migration_user_password=None, encrypted_replicator_database_user_password=None,
                     aws_iam_role_session_credentials=None, dry_run=True):
    timeout_seconds = 120
    end_time = datetime.utcnow() + timedelta(seconds=timeout_seconds)

    placeholders = {
        'master_user_password': using_kms_decrypt_or_default(
            encrypted_master_database_user_password,
            'postgres',
            aws_iam_role_session_credentials,
            aws_region
        ),
        'heartbeat_user_password': using_kms_decrypt_or_default(
            encrypted_heartbeat_database_user_password,
            'heartbeat',
            aws_iam_role_session_credentials,
            aws_region
        ),
        'publisher_user_password': using_kms_decrypt_or_default(
            encrypted_publisher_database_user_password,
            'publisher',
            aws_iam_role_session_credentials,
            aws_region
        ),
        'replicator_user_password': using_kms_decrypt_or_default(
            encrypted_replicator_database_user_password,
            'replicator',
            aws_iam_role_session_credentials,
            aws_region
        )
    }

    placeholder_arguments_string = ' '.join(
        map(
            lambda placeholder: '-placeholders.{placeholder_name}="{placeholder_value}"'.format(
                placeholder_name=placeholder[0], placeholder_value=re.sub(r"([\"!\\])", r"\\\1", placeholder[1])),
            placeholders.iteritems()
        )
    )

    database_migration_user_password = using_kms_decrypt_or_default(
        encrypted_database_migration_user_password,
        'postgres',
        aws_iam_role_session_credentials,
        aws_region
    )

    with settings(hide('running', 'warnings'), warn_only=True):
        while True:
            flyway_result = local(
                'flyway -user=postgres -password="{database_migration_user_password}" '
                '-url=jdbc:postgresql://authors-database.internal-service:5432/{database_name} '
                '{placeholder_arguments_string} '
                '-locations=filesystem:{migrations_directory_path} {flyway_command}'.format(
                    database_migration_user_password=re.sub(r"([\"!\\])", r"\\\1", database_migration_user_password),
                    database_name=database_name,
                    placeholder_arguments_string=placeholder_arguments_string,
                    migrations_directory_path=os.path.join(
                        service_directory_path, 'infrastructure/db-migration-scripts'
                    ),
                    flyway_command='info' if dry_run else 'migrate')
            )

            if flyway_result.succeeded:
                return

            if datetime.utcnow() > end_time:
                raise Exception('Failed to migrate postgres database '
                                'within {timeout_seconds} seconds.'.format(timeout_seconds=timeout_seconds))

            time.sleep(1)
