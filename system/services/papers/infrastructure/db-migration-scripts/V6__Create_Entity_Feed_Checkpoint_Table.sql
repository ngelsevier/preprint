CREATE TABLE job_checkpoint (
  job        VARCHAR(20) NOT NULL UNIQUE,
  checkpoint VARCHAR(50) NOT NULL
);