#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset

private_key_path="${1}"

openssl pkey -in "$(realpath ${private_key_path})" -pubout -outform DER | openssl md5 -c