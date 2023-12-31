CREATE database reviewboard;
\c reviewboard;

CREATE TABLE IF NOT EXISTS companies (
    id   BIGSERIAL PRIMARY KEY,
    slug TEXT UNIQUE NOT NULL,
    name TEXT UNIQUE NOT NULL,
    url  TEXT UNIQUE NOT NULL,
    location TEXT,
    country TEXT,
    industry TEXT,
    image TEXT,
    tags TEXT
);

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