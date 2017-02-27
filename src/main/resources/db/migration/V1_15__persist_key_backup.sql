DROP TABLE IF EXISTS key_backup;
CREATE TABLE key_backup (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  id_code BIGINT(20) NOT NULL,
  challenge CHAR(32),
  address CHAR(42),
  key_enc CHAR(44),
  active boolean,
  UNIQUE (challenge,address),
  PRIMARY KEY (id)
);
