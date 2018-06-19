import hashlib
import hmac
import json
import logging
import os
import requests
from datetime import timedelta, datetime

logger = logging.getLogger()
logger.setLevel(logging.INFO)

service = 'es'
host = os.environ['AWS_ES_HOST']
region = 'us-east-1'

access_index_name = 'frontend-accesslog'
default_threshold_date = datetime.today() - timedelta(days=int(os.environ['THRESHOLD_IN_DAYS']) + 1)
access_log_threshold_date = datetime.today() - timedelta(days=int(os.environ['ACCESS_LOG_THRESHOLD_IN_DAYS']) + 1)

aws_access_key_id = os.environ['AWS_ACCESS_KEY_ID']
aws_secret_access_key = os.environ['AWS_SECRET_ACCESS_KEY']
aws_session_token = os.environ['AWS_SESSION_TOKEN']


def lambda_handler(event, context):
    data = json.loads(sign_and_submit('GET', '/_aliases'))
    logstash = {k for k in data.iterkeys() if k.encode('utf-8').startswith('logstash')}
    for index_name in logstash:
        logger.debug('Checking {index}'.format(index=index_name))

        if access_index_name == index_name:
            threshold_date = access_log_threshold_date
        else:
            threshold_date = default_threshold_date

        if check_index_name(index_name, threshold_date):
            sign_and_submit('DELETE', '/{index}'.format(index=index_name))


def check_index_name(index_name, threshold_date):
    date = datetime.strptime(index_name.split('-')[1], '%Y.%m.%d')
    return date < threshold_date


def sign_and_submit(method, canonical_uri):
    endpoint = 'http://{host}/{canonical_uri}'.format(host=host, canonical_uri=canonical_uri)

    headers = generate_auth_headers(canonical_uri, method)

    request_url = endpoint + '?'
    if method == 'GET':
        r = requests.get(request_url, headers=headers)
    elif method == 'DELETE':
        r = requests.delete(request_url, headers=headers)

    if r.status_code != 200:
        error_message = 'Response code was %d with error %s' % (r.status_code, r.text)
        logger.error(error_message)
        raise Exception(error_message)

    return r.text


def generate_auth_headers(canonical_uri, method):
    t = datetime.utcnow()
    amzdate = t.strftime('%Y%m%dT%H%M%SZ')
    datestamp = t.strftime('%Y%m%d')
    canonical_headers = 'host:' + host + '\n' + 'x-amz-date:' + amzdate + '\n'
    signed_headers = 'host;x-amz-date'

    if aws_session_token != '':
        canonical_headers += 'x-amz-security-token:' + aws_session_token + '\n'
        signed_headers += ';x-amz-security-token'

    payload_hash = hashlib.sha256('').hexdigest()
    canonical_request = method + '\n' + canonical_uri + '\n\n' + canonical_headers + '\n' + signed_headers + '\n' + payload_hash
    algorithm = 'AWS4-HMAC-SHA256'
    credential_scope = datestamp + '/' + region + '/' + service + '/' + 'aws4_request'
    string_to_sign = algorithm + '\n' + amzdate + '\n' + credential_scope + '\n' + hashlib.sha256(
        canonical_request).hexdigest()
    signing_key = __get_signature_key(aws_secret_access_key, datestamp, region, service)
    signature = hmac.new(signing_key, string_to_sign.encode('utf-8'), hashlib.sha256).hexdigest()
    authorization_header = algorithm + ' ' + 'Credential=' + aws_access_key_id + '/' + credential_scope + ', ' + 'SignedHeaders=' + signed_headers + ', ' + 'Signature=' + signature

    headers = {'x-amz-date': amzdate, 'Authorization': authorization_header}
    if aws_session_token != '':
        headers['X-Amz-Security-Token'] = aws_session_token

    return headers


def __sign(key, msg):
    return hmac.new(key, msg.encode('utf-8'), hashlib.sha256).digest()


def __get_signature_key(key, date_stamp, region_name, service_name):
    k_date = __sign(('AWS4' + key).encode('utf-8'), date_stamp)
    k_region = __sign(k_date, region_name)
    k_service = __sign(k_region, service_name)
    k_signing = __sign(k_service, 'aws4_request')
    return k_signing


if __name__ == '__main__':
    logger.addHandler(logging.StreamHandler())
    lambda_handler(None, None)
