DROP TABLE IF EXISTS ldap_response;
CREATE TABLE ldap_response (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  id_code BIGINT(20) NOT NULL,
  first_name VARCHAR(250),
  last_name VARCHAR(250),
  UNIQUE (id_code),
  PRIMARY KEY (id)
);
