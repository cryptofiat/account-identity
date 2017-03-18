CREATE SCHEMA `account_identity` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Might fail if flyway user has already been created for that node
CREATE USER 'flyway'@'%' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON account_identity.* TO flyway;

CREATE USER 'acc-id-service'@'%' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON account_identity.* TO acc-id-service;

USE account_identity;