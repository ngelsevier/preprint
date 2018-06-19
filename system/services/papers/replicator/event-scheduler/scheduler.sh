#!/usr/bin/env bash

set -o pipefail -o nounset -o errexit

child_pid=0

term_handler() {
  if [ "${child_pid}" -ne 0 ]; then
    kill ${child_pid}
    wait ${child_pid}
  fi
  exit 143
}

trap 'term_handler' SIGTERM

while true
do
    echo "Scheduling job..."
    set +e
    timeout --foreground 60 python job.py &
    child_pid=${!}
    wait ${child_pid}
    set -e
done