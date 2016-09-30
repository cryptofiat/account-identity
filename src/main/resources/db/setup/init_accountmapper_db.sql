CREATE SCHEMA `account_identity` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Might fail if flyway user has already been created for that node
CREATE USER 'flyway'@'localhost' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON account_identity.* TO 'flyway'@'localhost';

CREATE USER 'acc-id-service'@'localhost' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON account_identity.* TO 'acc-id-service'@'localhost';

USE account_identity;