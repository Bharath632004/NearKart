-- NearKart: Initialize all service databases
CREATE DATABASE IF NOT EXISTS nearkart_auth;
CREATE DATABASE IF NOT EXISTS nearkart_user;
CREATE DATABASE IF NOT EXISTS nearkart_product;
CREATE DATABASE IF NOT EXISTS nearkart_shop;
CREATE DATABASE IF NOT EXISTS nearkart_order;
CREATE DATABASE IF NOT EXISTS nearkart_payment;
CREATE DATABASE IF NOT EXISTS nearkart_delivery;
CREATE DATABASE IF NOT EXISTS nearkart_notification;
CREATE DATABASE IF NOT EXISTS nearkart_inventory;
CREATE DATABASE IF NOT EXISTS nearkart_merchant;
CREATE DATABASE IF NOT EXISTS nearkart_analytics;
CREATE DATABASE IF NOT EXISTS nearkart_admin;

-- Grant all privileges to nearkart user on all databases
GRANT ALL PRIVILEGES ON nearkart_auth.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_user.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_product.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_shop.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_order.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_payment.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_delivery.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_notification.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_inventory.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_merchant.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_analytics.* TO 'nearkart'@'%';
GRANT ALL PRIVILEGES ON nearkart_admin.* TO 'nearkart'@'%';
FLUSH PRIVILEGES;
