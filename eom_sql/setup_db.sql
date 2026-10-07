CREATE DATABASE IF NOT EXISTS ecommerce_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'eom_app'@'localhost' IDENTIFIED WITH mysql_native_password BY 'EomApp_2026!';
GRANT ALL PRIVILEGES ON ecommerce_order.* TO 'eom_app'@'localhost';
GRANT CREATE ROUTINE, ALTER ROUTINE, EXECUTE ON ecommerce_order.* TO 'eom_app'@'localhost';
SET GLOBAL log_bin_trust_function_creators=1;
FLUSH PRIVILEGES;
SHOW DATABASES;
