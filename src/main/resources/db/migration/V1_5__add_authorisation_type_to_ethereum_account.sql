ALTER TABLE ethereum_account ADD authorisation_type VARCHAR(50) NOT NULL;
UPDATE ethereum_account SET authorisation_type = 'MOBILE_ID';
