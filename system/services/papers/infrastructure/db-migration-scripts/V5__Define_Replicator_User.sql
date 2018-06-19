DO
$body$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM pg_catalog.pg_user
      WHERE usename = 'replicator')
  THEN

    CREATE ROLE replicator WITH LOGIN ENCRYPTED PASSWORD 'replicator';
  END IF;
END
$body$;

GRANT INSERT, UPDATE, SELECT ON paper TO replicator;
GRANT USAGE, SELECT ON SEQUENCE paper_sequence_number_seq TO replicator;