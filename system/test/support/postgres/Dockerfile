FROM postgres:9.6.6
ENV POSTGRES_PASSWORD postgres
ENV PGDATA /var/lib/postgresql/data-ephemeral
RUN mkdir "${PGDATA}" && chown postgres:postgres "${PGDATA}"
COPY configure.sh /docker-entrypoint-initdb.d
COPY configure.sql /docker-entrypoint-initdb.d
