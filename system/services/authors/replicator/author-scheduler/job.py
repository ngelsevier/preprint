import requests
import os

EXPECTED_RESPONSE_CODES = [201, 409]
REPLICATOR_JOBS_URL = 'http://authors-replicator.internal-service/jobs/entity-replication'


def lambda_handler(event, context):
    payload = {
        'jobBatchSize': int(os.environ['JOB_BATCH_SIZE']),
        'databaseUpsertBatchSize': int(os.environ['DATABASE_UPSERT_BATCH_SIZE'])
    }

    response = requests.post(REPLICATOR_JOBS_URL, json=payload)
    actual_status_code = response.status_code

    if actual_status_code not in EXPECTED_RESPONSE_CODES:
        expected_response_codes_description = ' or '.join(map(lambda code: str(code), EXPECTED_RESPONSE_CODES))

        raise Exception(
            'Expected {expected_response_codes_description} response code from {url} '
            'but received {actual_status_code}'.format(
                url=REPLICATOR_JOBS_URL,
                actual_status_code=actual_status_code,
                expected_response_codes_description=expected_response_codes_description
            )
        )


if __name__ == '__main__':
    lambda_handler(None, None)
