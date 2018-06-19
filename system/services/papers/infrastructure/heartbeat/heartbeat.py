import boto3
import pg8000
import logging, datetime, time
from base64 import b64decode
import os

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def lambda_handler(event, context):
    cursor = None
    try:
        password = __decrypt_using_kms(os.environ['ENCRYPTED_DATABASE_PASSWORD'])
        conn = pg8000.connect(
            host='papers-database.internal-service',
            database='papers',
            user='heartbeat',
            password=password
        )

        cursor = conn.cursor()
        cursor.execute("DELETE FROM heartbeat")
        cursor.execute("INSERT INTO heartbeat VALUES (NOW())")
        conn.commit()
    finally:
        if cursor:
            cursor.close()


def __decrypt_using_kms(encrypted_text):
    client = boto3.client('kms')
    decrypt_response = client.decrypt(CiphertextBlob=(b64decode(encrypted_text)))
    return decrypt_response['Plaintext']


if __name__ == '__main__':
    logger.addHandler(logging.StreamHandler())
    lambda_handler(None, None)
