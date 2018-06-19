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

time_of_last_job_run=0
approximate_seconds_between_jobs=5

while true
do
    if [ $(($(date +%s)-${time_of_last_job_run})) -ge ${approximate_seconds_between_jobs} ]; then
        echo "Scheduling job..."
        set +e
        timeout --foreground ${approximate_seconds_between_jobs} python job.py &
        child_pid=${!}
        wait ${child_pid}
        set -e
        time_of_last_job_run=$(date +%s)
    fi
done