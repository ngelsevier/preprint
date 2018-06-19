DO
$body$
BEGIN
  IF NOT EXISTS(
      SELECT
      FROM pg_catalog.pg_user
      WHERE usename = 'publisher')
  THEN

    BEGIN
      IF (SELECT pg_shdescription.description = 'simulated environment'
          FROM pg_shdescription
            JOIN pg_database ON pg_shdescription.objoid = pg_database.oid
          WHERE pg_database.datname = 'authors')
      THEN
        CREATE ROLE publisher WITH REPLICATION LOGIN ENCRYPTED PASSWORD 'publisher';
      ELSE
        CREATE ROLE publisher WITH LOGIN ENCRYPTED PASSWORD 'publisher';
        GRANT rds_superuser TO publisher;
        GRANT rds_replication TO publisher;
      END IF;
    END;

  END IF;
END
$body$;