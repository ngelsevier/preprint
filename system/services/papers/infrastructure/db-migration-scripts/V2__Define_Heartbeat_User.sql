DO
$body$
BEGIN
   IF NOT EXISTS (
      SELECT
      FROM   pg_catalog.pg_user
      WHERE  usename = 'heartbeat') THEN

      CREATE ROLE heartbeat WITH LOGIN ENCRYPTED PASSWORD  'heartbeat';
   END IF;
END
$body$;

GRANT DELETE, INSERT ON heartbeat TO heartbeat;