CREATE SCHEMA `accountmapper` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Might fail if flyway user has already been created for that node
CREATE USER 'flyway'@'localhost' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON accountmapper.* TO 'flyway'@'localhost';

CREATE USER 'accountmapper-service'@'localhost' IDENTIFIED BY 'q1w2e3r4';
GRANT ALL PRIVILEGES ON accountmapper.* TO 'accountmapper-service'@'localhost';