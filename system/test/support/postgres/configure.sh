#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset
PGDATA='/var/lib/postgresql/data-ephemeral'

echo 'host replication all 0.0.0.0/0 md5' >> "${PGDATA}/pg_hba.conf"
echo 'max_wal_senders = 50' >> "${PGDATA}/postgresql.conf"
echo 'wal_level = logical' >> "${PGDATA}/postgresql.conf"
echo 'max_replication_slots = 45' >> "${PGDATA}/postgresql.conf"