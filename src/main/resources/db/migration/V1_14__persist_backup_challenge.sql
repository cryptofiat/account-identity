DROP TABLE IF EXISTS backup_challenge;
CREATE TABLE backup_challenge (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  id_code BIGINT(20) NOT NULL,
  plaintext CHAR(32),
  encrypted CHAR(32),
  active boolean,
  PRIMARY KEY (id)
);
