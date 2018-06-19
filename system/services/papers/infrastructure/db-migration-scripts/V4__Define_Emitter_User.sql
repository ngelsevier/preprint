DO
$body$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM pg_catalog.pg_user
      WHERE usename = 'emitter')
  THEN

    BEGIN
      IF (SELECT pg_shdescription.description = 'simulated environment'
          FROM pg_shdescription
            JOIN pg_database ON pg_shdescription.objoid = pg_database.oid
          WHERE pg_database.datname = 'papers')
      THEN
        CREATE ROLE emitter WITH REPLICATION LOGIN ENCRYPTED PASSWORD 'emitter';
      ELSE
        CREATE ROLE emitter WITH LOGIN ENCRYPTED PASSWORD 'emitter';
        GRANT rds_superuser TO emitter;
        GRANT rds_replication TO emitter;
      END IF;
    END;

  END IF;
END
$body$;