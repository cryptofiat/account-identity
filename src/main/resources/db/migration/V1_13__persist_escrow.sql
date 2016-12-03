CREATE TABLE escrow (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  id_code BIGINT(20) NOT NULL,
  private_key VARCHAR(250),
  address VARCHAR(250),
  cleared boolean,
  clearing_hash VARCHAR(250),
  PRIMARY KEY (id)
);
