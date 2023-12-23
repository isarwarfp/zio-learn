CREATE TABLE IF NOT EXISTS reviews (
  id  BIGSERIAL PRIMARY KEY,
  company_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  management INT,
  culture INT,
  salary INT,
  benefits INT,
  would_recommend INT,
  review TEXT,
  created TIMESTAMP NOT NULL DEFAULT now(),
  updated TIMESTAMP NOT NULL DEFAULT now()
);