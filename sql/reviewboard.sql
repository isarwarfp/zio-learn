CREATE database reviewboard;
\c reviewboard;

CREATE TABLE IF NOT EXISTS companies (
    id   BIGSERIAL PRIMARY KEY,
    slug TEXT NOT NULL,
    name TEXT NOT NULL,
    url  TEXT NOT NULL,
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

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL,
    hashed_pwd TEXT NOT NULL
);