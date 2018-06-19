import requests

SEARCH_STRING = 'Effects of the Real Plan on the Brazilian Banking System'
EXPECTED_RESULT_STRING = '<em>Effects</em> <em>of</em> <em>the</em> <em>Real</em> <em>Plan</em> <em>on</em> ' + \
                         '<em>the</em> <em>Brazilian</em> <em>Banking</em> <em>System</em>'


def lambda_handler(event, context):
    response = requests.get('https://www.ssrn.com/n/fastsearch',
                            params={'query': SEARCH_STRING},
                            auth=('ssrn-els', 'gEP8FuBY'))

    if response.status_code != 200:
        raise Exception('Expected response code was 200, but got "{actual_status_code}"'
                        .format(actual_status_code=(response.status_code)))

    extractedResult = response.text[response.text.index('class="paper"'):]

    if EXPECTED_RESULT_STRING not in extractedResult:
        raise Exception('Unable to find known paper with "{search_string}"'
                        .format(search_string=(SEARCH_STRING)))


if __name__ == '__main__':
    lambda_handler(None, None)
