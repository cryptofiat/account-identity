CREATE TABLE pending_authorisation (
  auth_identifier binary(16) NOT NULL,
  type VARCHAR(100) NOT NULL,
	address VARCHAR(50),
	serialised_mobile_id_session VARCHAR(100),
  creation_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  modification_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (auth_identifier)
);
