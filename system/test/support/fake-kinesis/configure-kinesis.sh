#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset

for stream_name in $@
do
    aws --endpoint-url https://localhost:4567 --no-verify-ssl --region us-east-1 \
        kinesis create-stream \
            --stream-name "${stream_name}" \
            --shard-count 1
done