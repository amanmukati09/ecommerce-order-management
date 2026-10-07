-- ============================================================
-- Ecommerce Order Management - Database Schema
-- MySQL 8.x - UTF8MB4
-- Monetary columns use DECIMAL(10,2) with NOT NULL constraints.
-- ============================================================

DROP DATABASE IF EXISTS ecommerce_order;
CREATE DATABASE ecommerce_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_order;

-- ============================================================
-- 1. CATEGORIES
-- ============================================================
CREATE TABLE categories (
    category_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    slug          VARCHAR(140) NOT NULL,
    description   VARCHAR(500),
    parent_id     BIGINT NULL,
    status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_categories_slug (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(category_id),
    INDEX idx_categories_status (status),
    INDEX idx_categories_parent (parent_id)
) ENGINE=InnoDB;

-- ============================================================
-- 2. CUSTOMERS
-- ============================================================
CREATE TABLE customers (
    customer_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_ref  VARCHAR(20) NOT NULL,
    first_name    VARCHAR(80) NOT NULL,
    last_name     VARCHAR(80) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone         VARCHAR(30),
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city          VARCHAR(100),
    region        VARCHAR(100),
    postcode      VARCHAR(20),
    country       VARCHAR(60) NOT NULL DEFAULT 'US',
    loyalty_tier  ENUM('STANDARD','SILVER','GOLD','PLATINUM') NOT NULL DEFAULT 'STANDARD',
    status        ENUM('ACTIVE','SUSPENDED','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customers_ref (customer_ref),
    UNIQUE KEY uk_customers_email (email),
    INDEX idx_customers_status (status),
    INDEX idx_customers_tier (loyalty_tier),
    INDEX idx_customers_name (last_name, first_name)
) ENGINE=InnoDB;

-- ============================================================
-- 3. WAREHOUSES
-- ============================================================
CREATE TABLE warehouses (
    warehouse_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(10) NOT NULL,
    name          VARCHAR(150) NOT NULL,
    region        VARCHAR(60) NOT NULL,
    city          VARCHAR(100),
    country       VARCHAR(60) NOT NULL DEFAULT 'US',
    status        ENUM('ACTIVE','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_warehouses_code (code),
    INDEX idx_warehouses_region (region)
) ENGINE=InnoDB;

-- ============================================================
-- 4. PRODUCTS
-- Monetary columns DECIMAL(10,2)
-- ============================================================
CREATE TABLE products (
    product_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku           VARCHAR(40) NOT NULL,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    category_id   BIGINT NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    cost          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    weight_kg     DECIMAL(8,3) NOT NULL DEFAULT 0.000,
    currency      CHAR(3) NOT NULL DEFAULT 'USD',
    rating_avg    DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_count  INT NOT NULL DEFAULT 0,
    status        ENUM('ACTIVE','DISCONTINUED','DRAFT') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_products_sku (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_status (status),
    INDEX idx_products_price (price),
    INDEX idx_products_name (name)
) ENGINE=InnoDB;

-- ============================================================
-- 5. INVENTORY (stock per product/warehouse)
-- ============================================================
CREATE TABLE inventory (
    inventory_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id     BIGINT NOT NULL,
    warehouse_id   BIGINT NOT NULL,
    quantity       INT NOT NULL DEFAULT 0,
    reserved       INT NOT NULL DEFAULT 0,
    reorder_level  INT NOT NULL DEFAULT 10,
    reorder_qty    INT NOT NULL DEFAULT 50,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory_product_wh (product_id, warehouse_id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_warehouse (warehouse_id),
    INDEX idx_inventory_low (quantity, reorder_level)
) ENGINE=InnoDB;

-- ============================================================
-- 6. DISCOUNT CODES
-- ============================================================
CREATE TABLE discount_codes (
    discount_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(40) NOT NULL,
    description    VARCHAR(300),
    discount_type  ENUM('PERCENT','FIXED') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_discount   DECIMAL(10,2) NULL,
    valid_from     DATETIME NOT NULL,
    valid_to       DATETIME NOT NULL,
    usage_limit    INT NULL,
    times_used     INT NOT NULL DEFAULT 0,
    status         ENUM('ACTIVE','EXPIRED','DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_discount_code (code),
    INDEX idx_discount_status (status),
    INDEX idx_discount_validity (valid_from, valid_to)
) ENGINE=InnoDB;

-- ============================================================
-- 7. CARTS
-- ============================================================
CREATE TABLE carts (
    cart_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT NOT NULL,
    status        ENUM('OPEN','CONVERTED','ABANDONED') NOT NULL DEFAULT 'OPEN',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    INDEX idx_carts_customer (customer_id),
    INDEX idx_carts_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- 8. CART ITEMS
-- ============================================================
CREATE TABLE cart_items (
    cart_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id       BIGINT NOT NULL,
    product_id    BIGINT NOT NULL,
    quantity      INT NOT NULL DEFAULT 1,
    unit_price    DECIMAL(10,2) NOT NULL,
    added_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_items_cart_product (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_cart_items_cart (cart_id),
    INDEX idx_cart_items_product (product_id)
) ENGINE=InnoDB;

-- ============================================================
-- 9. ORDERS
-- ============================================================
CREATE TABLE orders (
    order_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_ref       VARCHAR(30) NOT NULL,
    customer_id     BIGINT NOT NULL,
    warehouse_id    BIGINT NULL,
    discount_id     BIGINT NULL,
    status          ENUM('PLACED','PAID','SHIPPED','DELIVERED','CANCELLED','RETURNED','REFUNDED') NOT NULL DEFAULT 'PLACED',
    subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency        CHAR(3) NOT NULL DEFAULT 'USD',
    shipping_address VARCHAR(400),
    placed_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_orders_ref (order_ref),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    CONSTRAINT fk_orders_discount FOREIGN KEY (discount_id) REFERENCES discount_codes(discount_id),
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_placed (placed_at),
    INDEX idx_orders_customer_placed (customer_id, placed_at DESC)
) ENGINE=InnoDB;

-- ============================================================
-- 10. ORDER LINE ITEMS
-- ============================================================
CREATE TABLE order_items (
    order_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    product_id     BIGINT NOT NULL,
    sku            VARCHAR(40) NOT NULL,
    product_name   VARCHAR(200) NOT NULL,
    quantity       INT NOT NULL,
    unit_price     DECIMAL(10,2) NOT NULL,
    line_total     DECIMAL(10,2) NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id)
) ENGINE=InnoDB;

-- ============================================================
-- 11. PAYMENTS
-- ============================================================
CREATE TABLE payments (
    payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_ref    VARCHAR(30) NOT NULL,
    order_id       BIGINT NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    currency       CHAR(3) NOT NULL DEFAULT 'USD',
    method         ENUM('CARD','PAYPAL','BANK_TRANSFER','WALLET','COD') NOT NULL,
    status         ENUM('PENDING','AUTHORIZED','CAPTURED','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(60),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payments_ref (payment_ref),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_payments_order (order_id),
    INDEX idx_payments_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- 12. SHIPMENTS
-- ============================================================
CREATE TABLE shipments (
    shipment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_ref   VARCHAR(30) NOT NULL,
    order_id       BIGINT NOT NULL,
    warehouse_id   BIGINT NULL,
    carrier        VARCHAR(80),
    tracking_number VARCHAR(80),
    status         ENUM('PENDING','DISPATCHED','IN_TRANSIT','DELIVERED','RETURNED') NOT NULL DEFAULT 'PENDING',
    shipping_cost  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipped_at     DATETIME NULL,
    delivered_at   DATETIME NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_shipments_ref (shipment_ref),
    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_shipments_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    INDEX idx_shipments_order (order_id),
    INDEX idx_shipments_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- 13. RETURNS / REFUNDS
-- ============================================================
CREATE TABLE returns (
    return_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_ref     VARCHAR(30) NOT NULL,
    order_id       BIGINT NOT NULL,
    reason         VARCHAR(300),
    refund_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status         ENUM('REQUESTED','APPROVED','RECEIVED','REFUNDED','REJECTED') NOT NULL DEFAULT 'REQUESTED',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at    DATETIME NULL,
    UNIQUE KEY uk_returns_ref (return_ref),
    CONSTRAINT fk_returns_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_returns_order (order_id),
    INDEX idx_returns_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- 14. REVIEWS
-- ============================================================
CREATE TABLE reviews (
    review_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id     BIGINT NOT NULL,
    customer_id    BIGINT NOT NULL,
    rating         TINYINT NOT NULL,
    title          VARCHAR(200),
    body           TEXT,
    verified_purchase TINYINT(1) NOT NULL DEFAULT 0,
    status         ENUM('PUBLISHED','PENDING','REJECTED') NOT NULL DEFAULT 'PUBLISHED',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    UNIQUE KEY uk_reviews_product_customer (product_id, customer_id),
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_customer (customer_id),
    INDEX idx_reviews_rating (rating)
) ENGINE=InnoDB;

-- ============================================================
-- 15. ORDER STATUS HISTORY / AUDIT
-- ============================================================
CREATE TABLE order_status_history (
    history_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    old_status     VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    changed_by     VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    note           VARCHAR(300),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_osh_order (order_id),
    INDEX idx_osh_created (created_at)
) ENGINE=InnoDB;
