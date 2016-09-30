DROP TABLE IF EXISTS ethereum_account;
CREATE TABLE ethereum_account (
  id BIGINT(20) NOT NULL,
  owner_id VARCHAR(25) NOT NULL,
  address VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);