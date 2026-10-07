-- ============================================================
-- Ecommerce Order Management - Seed Data
-- 10 categories, 4 warehouses, 100+ products, 35 customers,
-- inventory (incl. low-stock items), discount codes, 150+ orders.
-- ============================================================
USE ecommerce_order;

-- ============================================================
-- CATEGORIES (10)
-- ============================================================
INSERT INTO categories (name, slug, description) VALUES
('Electronics','electronics','Phones, laptops, and gadgets'),
('Home & Kitchen','home-kitchen','Appliances and homeware'),
('Books','books','Printed and digital books'),
('Clothing','clothing','Apparel for all'),
('Sports & Outdoors','sports-outdoors','Fitness and outdoor gear'),
('Toys & Games','toys-games','Fun for all ages'),
('Beauty & Health','beauty-health','Personal care and wellness'),
('Automotive','automotive','Car parts and accessories'),
('Garden','garden','Plants, tools, and outdoor living'),
('Office','office','Stationery and office supplies');

-- ============================================================
-- WAREHOUSES (4 across regions)
-- ============================================================
INSERT INTO warehouses (code, name, region, city, country) VALUES
('WH-E','East Coast DC','EAST','Newark','US'),
('WH-W','West Coast DC','WEST','Oakland','US'),
('WH-C','Central DC','CENTRAL','Dallas','US'),
('WH-S','South DC','SOUTH','Atlanta','US');

-- ============================================================
-- CUSTOMERS (35)
-- ============================================================
INSERT INTO customers (customer_ref, first_name, last_name, email, phone, address_line1, city, region, postcode, country, loyalty_tier) VALUES
('C00001','Alice','Morgan','alice.morgan@example.com','+1-202-555-0101','12 Oak St','New York','NY','10001','US','GOLD'),
('C00002','Brian','Carter','brian.carter@example.com','+1-202-555-0102','45 Elm Ave','Los Angeles','CA','90001','US','SILVER'),
('C00003','Carla','Nguyen','carla.nguyen@example.com','+1-202-555-0103','7 Pine Rd','Chicago','IL','60601','US','PLATINUM'),
('C00004','David','Okafor','david.okafor@example.com','+1-202-555-0104','89 Maple Dr','Houston','TX','77001','US','STANDARD'),
('C00005','Elena','Rossi','elena.rossi@example.com','+1-202-555-0105','23 Birch Ln','Phoenix','AZ','85001','US','SILVER'),
('C00006','Frank','Walsh','frank.walsh@example.com','+1-202-555-0106','56 Cedar Ct','Philadelphia','PA','19019','US','STANDARD'),
('C00007','Grace','Kim','grace.kim@example.com','+1-202-555-0107','34 Willow Way','San Antonio','TX','78201','US','GOLD'),
('C00008','Henry','Patel','henry.patel@example.com','+1-202-555-0108','78 Ash Blvd','San Diego','CA','92101','US','STANDARD'),
('C00009','Isla','Fisher','isla.fisher@example.com','+1-202-555-0109','11 Beech St','Dallas','TX','75201','US','SILVER'),
('C00010','Jack','Reed','jack.reed@example.com','+1-202-555-0110','90 Spruce Ave','San Jose','CA','95101','US','PLATINUM'),
('C00011','Kira','Sato','kira.sato@example.com','+1-202-555-0111','15 Chestnut Rd','Austin','TX','73301','US','GOLD'),
('C00012','Liam','Brooks','liam.brooks@example.com','+1-202-555-0112','67 Hazel Dr','Jacksonville','FL','32099','US','STANDARD'),
('C00013','Maya','Lopez','maya.lopez@example.com','+1-202-555-0113','28 Rowan Ln','Columbus','OH','43085','US','SILVER'),
('C00014','Noah','Green','noah.green@example.com','+1-202-555-0114','52 Alder Ct','Charlotte','NC','28201','US','STANDARD'),
('C00015','Olivia','Shaw','olivia.shaw@example.com','+1-202-555-0115','9 Poplar Way','Indianapolis','IN','46201','US','GOLD'),
('C00016','Paul','Harris','paul.harris@example.com','+1-202-555-0116','41 Sycamore St','Seattle','WA','98101','US','STANDARD'),
('C00017','Quinn','Adams','quinn.adams@example.com','+1-202-555-0117','73 Walnut Ave','Denver','CO','80201','US','SILVER'),
('C00018','Rosa','Diaz','rosa.diaz@example.com','+1-202-555-0118','19 Hawthorn Rd','Washington','DC','20001','US','PLATINUM'),
('C00019','Sam','Turner','sam.turner@example.com','+1-202-555-0119','85 Juniper Dr','Boston','MA','02108','US','STANDARD'),
('C00020','Tina','Evans','tina.evans@example.com','+1-202-555-0120','32 Laurel Ln','El Paso','TX','79901','US','GOLD'),
('C00021','Umar','Khan','umar.khan@example.com','+1-202-555-0121','64 Fir Ct','Nashville','TN','37201','US','STANDARD'),
('C00022','Vera','Petrov','vera.petrov@example.com','+1-202-555-0122','47 Holly Way','Detroit','MI','48201','US','SILVER'),
('C00023','Will','Scott','will.scott@example.com','+1-202-555-0123','21 Ivy St','Portland','OR','97201','US','STANDARD'),
('C00024','Xena','Woods','xena.woods@example.com','+1-202-555-0124','58 Magnolia Ave','Memphis','TN','38101','US','GOLD'),
('C00025','Yusuf','Ali','yusuf.ali@example.com','+1-202-555-0125','13 Primrose Rd','Oklahoma City','OK','73101','US','STANDARD'),
('C00026','Zoe','Clark','zoe.clark@example.com','+1-202-555-0126','96 Daisy Dr','Las Vegas','NV','89101','US','SILVER'),
('C00027','Adam','Bauer','adam.bauer@example.com','+1-202-555-0127','29 Tulip Ln','Louisville','KY','40201','US','STANDARD'),
('C00028','Bella','Cruz','bella.cruz@example.com','+1-202-555-0128','72 Rose Ct','Baltimore','MD','21201','US','GOLD'),
('C00029','Caleb','Fox','caleb.fox@example.com','+1-202-555-0129','44 Lily Way','Milwaukee','WI','53201','US','STANDARD'),
('C00030','Dana','Hughes','dana.hughes@example.com','+1-202-555-0130','81 Violet St','Albuquerque','NM','87101','US','SILVER'),
('C00031','Eric','Stone','eric.stone@example.com','+1-202-555-0131','17 Orchid Ave','Tucson','AZ','85701','US','STANDARD'),
('C00032','Fiona','Blair','fiona.blair@example.com','+1-202-555-0132','53 Jasmine Rd','Fresno','CA','93650','US','GOLD'),
('C00033','Gabe','Munro','gabe.munro@example.com','+1-202-555-0133','26 Lotus Dr','Sacramento','CA','94203','US','STANDARD'),
('C00034','Hana','Yoshida','hana.yoshida@example.com','+1-202-555-0134','68 Dahlia Ln','Kansas City','MO','64101','US','SILVER'),
('C00035','Ian','Fletcher','ian.fletcher@example.com','+1-202-555-0135','35 Peony Ct','Mesa','AZ','85201','US','PLATINUM');

-- ============================================================
-- DISCOUNT CODES
-- ============================================================
INSERT INTO discount_codes (code, description, discount_type, discount_value, min_order_amount, max_discount, valid_from, valid_to, usage_limit) VALUES
('WELCOME10','10% off first order','PERCENT',10.00,0.00,50.00,'2026-01-01 00:00:00','2027-12-31 23:59:59',NULL),
('SAVE20','20% off orders over $100','PERCENT',20.00,100.00,100.00,'2026-01-01 00:00:00','2027-12-31 23:59:59',NULL),
('FLAT15','$15 off orders over $75','FIXED',15.00,75.00,NULL,'2026-01-01 00:00:00','2027-12-31 23:59:59',NULL),
('FREESHIP','$10 off shipping','FIXED',10.00,0.00,NULL,'2026-01-01 00:00:00','2027-12-31 23:59:59',1000),
('SUMMER25','25% summer sale','PERCENT',25.00,50.00,150.00,'2026-06-01 00:00:00','2026-08-31 23:59:59',500),
('EXPIRED5','Expired test code','PERCENT',5.00,0.00,NULL,'2025-01-01 00:00:00','2025-12-31 23:59:59',NULL),
('VIP30','30% VIP exclusive','PERCENT',30.00,200.00,300.00,'2026-01-01 00:00:00','2027-12-31 23:59:59',100);

UPDATE discount_codes SET status='EXPIRED' WHERE code='EXPIRED5';

-- ============================================================
-- PRODUCTS (120) - generated across the 10 categories
-- Uses a temporary seeding procedure for consistency, then drops it.
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_cat BIGINT;
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_cost DECIMAL(10,2);
    DECLARE v_name VARCHAR(200);
    DECLARE v_sku VARCHAR(40);
    WHILE i <= 120 DO
        SET v_cat = ((i - 1) % 10) + 1;
        SET v_price = ROUND(5 + (RAND() * 495), 2);
        SET v_cost = ROUND(v_price * (0.4 + RAND() * 0.3), 2);
        SET v_sku = CONCAT('SKU-', LPAD(i, 5, '0'));
        SET v_name = CONCAT(
            CASE v_cat
                WHEN 1 THEN 'Electronics Item '
                WHEN 2 THEN 'Kitchen Item '
                WHEN 3 THEN 'Book Title '
                WHEN 4 THEN 'Apparel Item '
                WHEN 5 THEN 'Sports Gear '
                WHEN 6 THEN 'Toy Item '
                WHEN 7 THEN 'Beauty Item '
                WHEN 8 THEN 'Auto Part '
                WHEN 9 THEN 'Garden Item '
                ELSE 'Office Item '
            END, i);
        INSERT INTO products (sku, name, description, category_id, price, cost, weight_kg)
        VALUES (v_sku, v_name, CONCAT('Quality ', v_name, ' with great value.'),
                v_cat, v_price, v_cost, ROUND(0.1 + RAND() * 5, 3));
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_products();
DROP PROCEDURE _seed_products;

-- Give a few products known high ratings for top-rated testing
UPDATE products SET rating_avg = 4.80, rating_count = 25 WHERE product_id = 1;
UPDATE products SET rating_avg = 4.60, rating_count = 40 WHERE product_id = 5;
UPDATE products SET rating_avg = 4.90, rating_count = 15 WHERE product_id = 11;
UPDATE products SET rating_avg = 4.70, rating_count = 32 WHERE product_id = 21;
UPDATE products SET status = 'DISCONTINUED' WHERE product_id IN (118, 119, 120);

-- ============================================================
-- INVENTORY - each product stocked in 1-2 warehouses.
-- A set of products are intentionally low-stock for the report.
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_inventory()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_wh BIGINT;
    DECLARE v_wh2 BIGINT;
    DECLARE v_qty INT;
    WHILE i <= 120 DO
        SET v_wh = ((i - 1) % 4) + 1;
        -- intentionally low stock for products 30-39
        IF i BETWEEN 30 AND 39 THEN
            SET v_qty = FLOOR(RAND() * 6); -- 0-5, below reorder level
        ELSE
            SET v_qty = 50 + FLOOR(RAND() * 450);
        END IF;
        INSERT INTO inventory (product_id, warehouse_id, quantity, reorder_level, reorder_qty)
        VALUES (i, v_wh, v_qty, 10, 50);
        -- some products also stocked in a second warehouse
        IF i % 3 = 0 THEN
            SET v_wh2 = (v_wh % 4) + 1;
            INSERT INTO inventory (product_id, warehouse_id, quantity, reorder_level, reorder_qty)
            VALUES (i, v_wh2, 20 + FLOOR(RAND() * 200), 10, 50);
        END IF;
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_inventory();
DROP PROCEDURE _seed_inventory;

-- ============================================================
-- HISTORICAL ORDERS (160) with line items, varied statuses.
-- Generated directly (not via sp_place_order) so we can set
-- historical dates and statuses without affecting live inventory.
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_orders()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_cust BIGINT;
    DECLARE v_status VARCHAR(15);
    DECLARE v_order_id BIGINT;
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_discount DECIMAL(10,2);
    DECLARE v_tax DECIMAL(10,2);
    DECLARE v_ship DECIMAL(10,2);
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_lines INT;
    DECLARE j INT;
    DECLARE v_prod BIGINT;
    DECLARE v_qty INT;
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_name VARCHAR(200);
    DECLARE v_sku VARCHAR(40);
    DECLARE v_days INT;

    WHILE i <= 160 DO
        SET v_cust = ((i - 1) % 35) + 1;
        -- distribute statuses
        SET v_status = CASE (i % 10)
            WHEN 0 THEN 'PLACED'
            WHEN 1 THEN 'PAID'
            WHEN 2 THEN 'PAID'
            WHEN 3 THEN 'SHIPPED'
            WHEN 4 THEN 'SHIPPED'
            WHEN 5 THEN 'DELIVERED'
            WHEN 6 THEN 'DELIVERED'
            WHEN 7 THEN 'DELIVERED'
            WHEN 8 THEN 'CANCELLED'
            ELSE 'RETURNED' END;
        SET v_days = FLOOR(RAND() * 120);
        SET v_ship = ROUND(5 + RAND() * 15, 2);

        INSERT INTO orders (order_ref, customer_id, warehouse_id, status, subtotal,
                            discount_amount, tax_amount, shipping_amount, total_amount,
                            shipping_address, placed_at)
        VALUES (CONCAT('ORD2026', LPAD(i, 6, '0')), v_cust, ((i - 1) % 4) + 1, v_status,
                0.00, 0.00, 0.00, v_ship, 0.00, 'Seed address', DATE_SUB(NOW(), INTERVAL v_days DAY));
        SET v_order_id = LAST_INSERT_ID();

        -- 1-4 line items
        SET v_lines = 1 + FLOOR(RAND() * 4);
        SET v_subtotal = 0.00;
        SET j = 1;
        WHILE j <= v_lines DO
            SET v_prod = 1 + FLOOR(RAND() * 120);
            SET v_qty = 1 + FLOOR(RAND() * 3);
            SELECT price, name, sku INTO v_price, v_name, v_sku FROM products WHERE product_id = v_prod;
            INSERT INTO order_items (order_id, product_id, sku, product_name, quantity, unit_price, line_total)
            VALUES (v_order_id, v_prod, v_sku, v_name, v_qty, v_price, ROUND(v_qty * v_price, 2));
            SET v_subtotal = v_subtotal + ROUND(v_qty * v_price, 2);
            SET j = j + 1;
        END WHILE;

        SET v_discount = IF(i % 5 = 0, ROUND(v_subtotal * 0.10, 2), 0.00);
        SET v_tax = ROUND((v_subtotal - v_discount) * 0.08, 2);
        SET v_total = (v_subtotal - v_discount) + v_tax + v_ship;
        UPDATE orders SET subtotal = v_subtotal, discount_amount = v_discount,
            tax_amount = v_tax, total_amount = v_total WHERE order_id = v_order_id;

        INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
        VALUES (v_order_id, NULL, v_status, 'SEED', 'Seeded historical order');

        -- payments for paid+ orders
        IF v_status IN ('PAID','SHIPPED','DELIVERED','RETURNED') THEN
            INSERT INTO payments (payment_ref, order_id, amount, method, status, transaction_id)
            VALUES (CONCAT('PAY2026', LPAD(i, 6, '0')), v_order_id, v_total, 'CARD', 'CAPTURED',
                    CONCAT('TXN', LPAD(i, 8, '0')));
        END IF;

        -- shipments for shipped+ orders
        IF v_status IN ('SHIPPED','DELIVERED','RETURNED') THEN
            INSERT INTO shipments (shipment_ref, order_id, warehouse_id, carrier, tracking_number,
                                   status, shipping_cost, shipped_at)
            VALUES (CONCAT('SHP2026', LPAD(i, 6, '0')), v_order_id, ((i - 1) % 4) + 1, 'UPS',
                    CONCAT('1Z', LPAD(i, 10, '0')),
                    IF(v_status = 'DELIVERED','DELIVERED','DISPATCHED'),
                    v_ship, DATE_SUB(NOW(), INTERVAL (v_days - 1) DAY));
        END IF;

        -- returns for returned orders
        IF v_status = 'RETURNED' THEN
            INSERT INTO returns (return_ref, order_id, reason, refund_amount, status)
            VALUES (CONCAT('RET2026', LPAD(i, 6, '0')), v_order_id, 'Customer changed mind',
                    v_total, 'APPROVED');
        END IF;

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_orders();
DROP PROCEDURE _seed_orders;

-- ============================================================
-- REVIEWS - a batch of published reviews across products
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_reviews()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_prod BIGINT;
    DECLARE v_cust BIGINT;
    DECLARE v_rating TINYINT;
    WHILE i <= 80 DO
        SET v_prod = 1 + ((i - 1) % 60);
        SET v_cust = 1 + ((i * 7) % 35);
        SET v_rating = 3 + FLOOR(RAND() * 3); -- 3-5
        INSERT IGNORE INTO reviews (product_id, customer_id, rating, title, body, verified_purchase, status)
        VALUES (v_prod, v_cust, v_rating,
                CONCAT('Review ', i), 'Solid product, would recommend.', 1, 'PUBLISHED');
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_reviews();
DROP PROCEDURE _seed_reviews;

-- recalc ratings from the seeded reviews for affected products
UPDATE products p
SET rating_avg = COALESCE((SELECT AVG(rating) FROM reviews r WHERE r.product_id = p.product_id AND r.status='PUBLISHED'), p.rating_avg),
    rating_count = COALESCE((SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.product_id AND r.status='PUBLISHED'), p.rating_count)
WHERE p.product_id IN (SELECT DISTINCT product_id FROM reviews);

-- A couple of open carts with items for cart testing
INSERT INTO carts (customer_id, status) VALUES (1,'OPEN'),(2,'OPEN'),(3,'OPEN');
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
SELECT 1, 1, 2, price FROM products WHERE product_id = 1;
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
SELECT 1, 2, 1, price FROM products WHERE product_id = 2;
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
SELECT 2, 5, 3, price FROM products WHERE product_id = 5;

-- ============================================================
-- SECOND WAVE ORDERS (additional 80 historical orders)
-- Deepens the order history for richer reporting/analytics.
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_orders2()
BEGIN
    DECLARE i INT DEFAULT 161;
    DECLARE v_cust BIGINT;
    DECLARE v_status VARCHAR(15);
    DECLARE v_order_id BIGINT;
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_discount DECIMAL(10,2);
    DECLARE v_tax DECIMAL(10,2);
    DECLARE v_ship DECIMAL(10,2);
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_lines INT;
    DECLARE j INT;
    DECLARE v_prod BIGINT;
    DECLARE v_qty INT;
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_name VARCHAR(200);
    DECLARE v_sku VARCHAR(40);
    DECLARE v_days INT;

    WHILE i <= 240 DO
        SET v_cust = ((i - 1) % 35) + 1;
        SET v_status = CASE (i % 8)
            WHEN 0 THEN 'PLACED'
            WHEN 1 THEN 'PAID'
            WHEN 2 THEN 'SHIPPED'
            WHEN 3 THEN 'DELIVERED'
            WHEN 4 THEN 'DELIVERED'
            WHEN 5 THEN 'DELIVERED'
            WHEN 6 THEN 'CANCELLED'
            ELSE 'RETURNED' END;
        SET v_days = 120 + FLOOR(RAND() * 120);
        SET v_ship = ROUND(5 + RAND() * 15, 2);

        INSERT INTO orders (order_ref, customer_id, warehouse_id, status, subtotal,
                            discount_amount, tax_amount, shipping_amount, total_amount,
                            shipping_address, placed_at)
        VALUES (CONCAT('ORD2026W2', LPAD(i, 6, '0')), v_cust, ((i - 1) % 4) + 1, v_status,
                0.00, 0.00, 0.00, v_ship, 0.00, 'Seed address W2', DATE_SUB(NOW(), INTERVAL v_days DAY));
        SET v_order_id = LAST_INSERT_ID();

        SET v_lines = 1 + FLOOR(RAND() * 4);
        SET v_subtotal = 0.00;
        SET j = 1;
        WHILE j <= v_lines DO
            SET v_prod = 1 + FLOOR(RAND() * 120);
            SET v_qty = 1 + FLOOR(RAND() * 3);
            SELECT price, name, sku INTO v_price, v_name, v_sku FROM products WHERE product_id = v_prod;
            INSERT INTO order_items (order_id, product_id, sku, product_name, quantity, unit_price, line_total)
            VALUES (v_order_id, v_prod, v_sku, v_name, v_qty, v_price, ROUND(v_qty * v_price, 2));
            SET v_subtotal = v_subtotal + ROUND(v_qty * v_price, 2);
            SET j = j + 1;
        END WHILE;

        SET v_discount = IF(i % 4 = 0, ROUND(v_subtotal * 0.15, 2), 0.00);
        SET v_tax = ROUND((v_subtotal - v_discount) * 0.08, 2);
        SET v_total = (v_subtotal - v_discount) + v_tax + v_ship;
        UPDATE orders SET subtotal = v_subtotal, discount_amount = v_discount,
            tax_amount = v_tax, total_amount = v_total WHERE order_id = v_order_id;

        INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
        VALUES (v_order_id, NULL, v_status, 'SEED', 'Seeded wave-2 historical order');

        IF v_status IN ('PAID','SHIPPED','DELIVERED','RETURNED') THEN
            INSERT INTO payments (payment_ref, order_id, amount, method, status, transaction_id)
            VALUES (CONCAT('PAY2026W2', LPAD(i, 6, '0')), v_order_id, v_total,
                    CASE (i % 3) WHEN 0 THEN 'CARD' WHEN 1 THEN 'PAYPAL' ELSE 'WALLET' END,
                    'CAPTURED', CONCAT('TXNW2', LPAD(i, 8, '0')));
        END IF;

        IF v_status IN ('SHIPPED','DELIVERED','RETURNED') THEN
            INSERT INTO shipments (shipment_ref, order_id, warehouse_id, carrier, tracking_number,
                                   status, shipping_cost, shipped_at)
            VALUES (CONCAT('SHP2026W2', LPAD(i, 6, '0')), v_order_id, ((i - 1) % 4) + 1,
                    CASE (i % 3) WHEN 0 THEN 'UPS' WHEN 1 THEN 'FedEx' ELSE 'DHL' END,
                    CONCAT('1ZW2', LPAD(i, 10, '0')),
                    IF(v_status = 'DELIVERED','DELIVERED','DISPATCHED'),
                    v_ship, DATE_SUB(NOW(), INTERVAL (v_days - 1) DAY));
        END IF;

        IF v_status = 'RETURNED' THEN
            INSERT INTO returns (return_ref, order_id, reason, refund_amount, status)
            VALUES (CONCAT('RET2026W2', LPAD(i, 6, '0')), v_order_id,
                    'Item not as described', v_total, 'REFUNDED');
        END IF;

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_orders2();
DROP PROCEDURE _seed_orders2;

-- ============================================================
-- ADDITIONAL REVIEWS (products 61-110)
-- ============================================================
DELIMITER $$
CREATE PROCEDURE _seed_reviews2()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_prod BIGINT;
    DECLARE v_cust BIGINT;
    DECLARE v_rating TINYINT;
    WHILE i <= 60 DO
        SET v_prod = 61 + ((i - 1) % 50);
        SET v_cust = 1 + ((i * 11) % 35);
        SET v_rating = 3 + FLOOR(RAND() * 3);
        INSERT IGNORE INTO reviews (product_id, customer_id, rating, title, body, verified_purchase, status)
        VALUES (v_prod, v_cust, v_rating,
                CONCAT('W2 Review ', i), 'Would buy again.', 1, 'PUBLISHED');
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL _seed_reviews2();
DROP PROCEDURE _seed_reviews2;

UPDATE products p
SET rating_avg = COALESCE((SELECT AVG(rating) FROM reviews r WHERE r.product_id = p.product_id AND r.status='PUBLISHED'), p.rating_avg),
    rating_count = COALESCE((SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.product_id AND r.status='PUBLISHED'), p.rating_count)
WHERE p.product_id IN (SELECT DISTINCT product_id FROM reviews);
