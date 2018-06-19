CREATE TABLE paper (
  sequence_number BIGSERIAL                NOT NULL,
  id              VARCHAR(50)              NOT NULL UNIQUE,
  entity          JSONB                    NOT NULL,
  last_updated    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  PRIMARY KEY (sequence_number)
);