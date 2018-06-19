import time
import requests
from datetime import datetime, timedelta
import logging
import os

logger = logging.getLogger()
logger.setLevel(logging.INFO)
EXPECTED_RESPONSE_CODES = [201, 409]
REPLICATOR_JOBS_URL = 'http://papers-replicator.internal-service/jobs/event-replication'
DEFAULT_SECONDS_BETWEEN_SCHEDULING_JOBS = 0.5


def lambda_handler(event, context):
    end_time = datetime.now() + timedelta(seconds=60)
    seconds_between_scheduling_jobs = float(os.environ['SECONDS_BETWEEN_SCHEDULING_JOBS']) \
        if 'SECONDS_BETWEEN_SCHEDULING_JOBS' in os.environ else DEFAULT_SECONDS_BETWEEN_SCHEDULING_JOBS

    while datetime.now() < end_time:
        response = requests.post(REPLICATOR_JOBS_URL)

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

        time.sleep(seconds_between_scheduling_jobs)


if __name__ == '__main__':
    logger.addHandler(logging.StreamHandler())
    lambda_handler(None, None)
