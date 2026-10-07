-- ============================================================
-- Ecommerce Order Management - Stored Procedures and Functions
-- MySQL 8.x - 85 procedures/functions
-- ============================================================
--
-- CONVENTIONS
-- -----------
-- * Every CREATE PROCEDURE / FUNCTION is wrapped in DELIMITER $$ ... DELIMITER ;
-- * Read procedures return result sets via a plain SELECT in the body
--   (consumed from Java via jdbcTemplate.query + CALL syntax).
-- * Write procedures returning a generated id use a single OUT parameter.
-- * Scalar lookups use CREATE FUNCTION ... RETURNS <scalar> (e.g.
--   fn_discounted_price computes a discounted price for a product + code).
-- * Order placement (sp_place_order) performs the full inventory check,
--   stock decrement, order + line item creation, and discount application
--   atomically inside one procedure using START TRANSACTION / COMMIT with a
--   ROLLBACK handler and RESIGNAL.
-- * Business-rule violations raise SIGNAL SQLSTATE '45000' with a prefixed
--   MESSAGE_TEXT. The Java DAO layer translates these to typed exceptions.
--
-- SIGNAL error message prefixes (parsed by Java SqlErrorTranslator):
--   INSUFFICIENT_STOCK   -> InsufficientStockException
--   DISCOUNT_INVALID     -> DiscountInvalidException
--   INVALID_STATUS       -> InvalidStatusTransitionException
--   NOT_FOUND            -> ResourceNotFoundException
--   INVALID_AMOUNT       -> InvalidAmountException
--   PAYMENT_ERROR        -> PaymentException
-- ============================================================
USE ecommerce_order;

-- ============================================================
-- CATEGORY PROCEDURES (1-7)
-- ============================================================

-- 1. sp_create_category
DELIMITER $$
CREATE PROCEDURE sp_create_category(
    IN p_name VARCHAR(120),
    IN p_slug VARCHAR(140),
    IN p_description VARCHAR(500),
    IN p_parent_id BIGINT,
    OUT p_category_id BIGINT)
BEGIN
    INSERT INTO categories (name, slug, description, parent_id)
    VALUES (p_name, p_slug, p_description, p_parent_id);
    SET p_category_id = LAST_INSERT_ID();
END$$
DELIMITER ;

-- 2. sp_get_category_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_category_by_id(IN p_category_id BIGINT)
BEGIN
    SELECT category_id, name, slug, description, parent_id, status, created_at
    FROM categories WHERE category_id = p_category_id;
END$$
DELIMITER ;

-- 3. sp_get_all_categories
DELIMITER $$
CREATE PROCEDURE sp_get_all_categories()
BEGIN
    SELECT category_id, name, slug, description, parent_id, status, created_at
    FROM categories WHERE status = 'ACTIVE' ORDER BY name;
END$$
DELIMITER ;

-- 4. sp_get_subcategories
DELIMITER $$
CREATE PROCEDURE sp_get_subcategories(IN p_parent_id BIGINT)
BEGIN
    SELECT category_id, name, slug, description, parent_id, status, created_at
    FROM categories WHERE parent_id = p_parent_id AND status = 'ACTIVE' ORDER BY name;
END$$
DELIMITER ;

-- 5. sp_update_category_status
DELIMITER $$
CREATE PROCEDURE sp_update_category_status(IN p_category_id BIGINT, IN p_status VARCHAR(10))
BEGIN
    UPDATE categories SET status = p_status WHERE category_id = p_category_id;
END$$
DELIMITER ;

-- 6. fn_get_category_product_count
DELIMITER $$
CREATE FUNCTION fn_get_category_product_count(p_category_id BIGINT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count FROM products WHERE category_id = p_category_id AND status = 'ACTIVE';
    RETURN v_count;
END$$
DELIMITER ;

-- 7. sp_get_category_tree
DELIMITER $$
CREATE PROCEDURE sp_get_category_tree()
BEGIN
    SELECT c.category_id, c.name, c.slug, c.parent_id,
           p.name AS parent_name,
           fn_get_category_product_count(c.category_id) AS product_count
    FROM categories c
    LEFT JOIN categories p ON c.parent_id = p.category_id
    WHERE c.status = 'ACTIVE'
    ORDER BY COALESCE(c.parent_id, c.category_id), c.name;
END$$
DELIMITER ;

-- ============================================================
-- CUSTOMER PROCEDURES (8-16)
-- ============================================================

-- 8. sp_create_customer
DELIMITER $$
CREATE PROCEDURE sp_create_customer(
    IN p_customer_ref VARCHAR(20),
    IN p_first_name VARCHAR(80),
    IN p_last_name VARCHAR(80),
    IN p_email VARCHAR(150),
    IN p_phone VARCHAR(30),
    IN p_address_line1 VARCHAR(200),
    IN p_city VARCHAR(100),
    IN p_region VARCHAR(100),
    IN p_postcode VARCHAR(20),
    IN p_country VARCHAR(60),
    OUT p_customer_id BIGINT)
BEGIN
    INSERT INTO customers (customer_ref, first_name, last_name, email, phone,
                           address_line1, city, region, postcode, country)
    VALUES (p_customer_ref, p_first_name, p_last_name, p_email, p_phone,
            p_address_line1, p_city, p_region, p_postcode, COALESCE(p_country, 'US'));
    SET p_customer_id = LAST_INSERT_ID();
END$$
DELIMITER ;

-- 9. sp_get_customer_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_customer_by_id(IN p_customer_id BIGINT)
BEGIN
    SELECT customer_id, customer_ref, first_name, last_name, email, phone,
           address_line1, address_line2, city, region, postcode, country,
           loyalty_tier, status, created_at
    FROM customers WHERE customer_id = p_customer_id;
END$$
DELIMITER ;

-- 10. sp_get_customer_by_email
DELIMITER $$
CREATE PROCEDURE sp_get_customer_by_email(IN p_email VARCHAR(150))
BEGIN
    SELECT customer_id, customer_ref, first_name, last_name, email, phone,
           loyalty_tier, status, created_at
    FROM customers WHERE email = p_email;
END$$
DELIMITER ;

-- 11. sp_update_customer_contact
DELIMITER $$
CREATE PROCEDURE sp_update_customer_contact(
    IN p_customer_id BIGINT,
    IN p_phone VARCHAR(30),
    IN p_address_line1 VARCHAR(200),
    IN p_city VARCHAR(100),
    IN p_region VARCHAR(100),
    IN p_postcode VARCHAR(20))
BEGIN
    UPDATE customers
    SET phone = COALESCE(p_phone, phone),
        address_line1 = COALESCE(p_address_line1, address_line1),
        city = COALESCE(p_city, city),
        region = COALESCE(p_region, region),
        postcode = COALESCE(p_postcode, postcode)
    WHERE customer_id = p_customer_id;
END$$
DELIMITER ;

-- 12. sp_update_customer_tier
DELIMITER $$
CREATE PROCEDURE sp_update_customer_tier(IN p_customer_id BIGINT, IN p_tier VARCHAR(10))
BEGIN
    UPDATE customers SET loyalty_tier = p_tier WHERE customer_id = p_customer_id;
END$$
DELIMITER ;

-- 13. sp_search_customers
DELIMITER $$
CREATE PROCEDURE sp_search_customers(IN p_query VARCHAR(100), IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT customer_id, customer_ref, first_name, last_name, email, phone,
           loyalty_tier, status, created_at
    FROM customers
    WHERE first_name LIKE CONCAT('%', p_query, '%')
       OR last_name LIKE CONCAT('%', p_query, '%')
       OR email LIKE CONCAT('%', p_query, '%')
       OR customer_ref LIKE CONCAT('%', p_query, '%')
    ORDER BY last_name, first_name
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 14. fn_get_customer_order_count
DELIMITER $$
CREATE FUNCTION fn_get_customer_order_count(p_customer_id BIGINT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count FROM orders WHERE customer_id = p_customer_id;
    RETURN v_count;
END$$
DELIMITER ;

-- 15. fn_get_customer_lifetime_value
DELIMITER $$
CREATE FUNCTION fn_get_customer_lifetime_value(p_customer_id BIGINT)
RETURNS DECIMAL(12,2)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(12,2) DEFAULT 0.00;
    SELECT COALESCE(SUM(total_amount), 0.00) INTO v_total
    FROM orders
    WHERE customer_id = p_customer_id
      AND status IN ('PAID','SHIPPED','DELIVERED');
    RETURN v_total;
END$$
DELIMITER ;

-- 16. sp_update_customer_status
DELIMITER $$
CREATE PROCEDURE sp_update_customer_status(IN p_customer_id BIGINT, IN p_status VARCHAR(10))
BEGIN
    UPDATE customers SET status = p_status WHERE customer_id = p_customer_id;
END$$
DELIMITER ;

-- ============================================================
-- PRODUCT PROCEDURES (17-28)
-- ============================================================

-- 17. sp_create_product
DELIMITER $$
CREATE PROCEDURE sp_create_product(
    IN p_sku VARCHAR(40),
    IN p_name VARCHAR(200),
    IN p_description TEXT,
    IN p_category_id BIGINT,
    IN p_price DECIMAL(10,2),
    IN p_cost DECIMAL(10,2),
    IN p_weight_kg DECIMAL(8,3),
    OUT p_product_id BIGINT)
BEGIN
    IF p_price < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: price cannot be negative';
    END IF;
    INSERT INTO products (sku, name, description, category_id, price, cost, weight_kg)
    VALUES (p_sku, p_name, p_description, p_category_id, p_price, p_cost, COALESCE(p_weight_kg, 0.000));
    SET p_product_id = LAST_INSERT_ID();
END$$
DELIMITER ;

-- 18. sp_get_product_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_product_by_id(IN p_product_id BIGINT)
BEGIN
    SELECT p.product_id, p.sku, p.name, p.description, p.category_id, p.price, p.cost,
           p.weight_kg, p.currency, p.rating_avg, p.rating_count, p.status,
           p.created_at, p.updated_at, c.name AS category_name
    FROM products p
    JOIN categories c ON p.category_id = c.category_id
    WHERE p.product_id = p_product_id;
END$$
DELIMITER ;

-- 19. sp_get_product_by_sku
DELIMITER $$
CREATE PROCEDURE sp_get_product_by_sku(IN p_sku VARCHAR(40))
BEGIN
    SELECT product_id, sku, name, description, category_id, price, cost,
           weight_kg, currency, rating_avg, rating_count, status, created_at, updated_at
    FROM products WHERE sku = p_sku;
END$$
DELIMITER ;

-- 20. sp_search_products_by_category
DELIMITER $$
CREATE PROCEDURE sp_search_products_by_category(IN p_category_id BIGINT, IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT p.product_id, p.sku, p.name, p.description, p.category_id, p.price, p.cost,
           p.weight_kg, p.currency, p.rating_avg, p.rating_count, p.status,
           p.created_at, p.updated_at, c.name AS category_name
    FROM products p
    JOIN categories c ON p.category_id = c.category_id
    WHERE p.category_id = p_category_id AND p.status = 'ACTIVE'
    ORDER BY p.name
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 21. sp_search_products
DELIMITER $$
CREATE PROCEDURE sp_search_products(IN p_query VARCHAR(200), IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT p.product_id, p.sku, p.name, p.description, p.category_id, p.price,
           p.rating_avg, p.rating_count, p.status, c.name AS category_name
    FROM products p
    JOIN categories c ON p.category_id = c.category_id
    WHERE p.status = 'ACTIVE'
      AND (p.name LIKE CONCAT('%', p_query, '%') OR p.sku LIKE CONCAT('%', p_query, '%'))
    ORDER BY p.rating_avg DESC, p.name
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 22. sp_update_product_price
DELIMITER $$
CREATE PROCEDURE sp_update_product_price(IN p_product_id BIGINT, IN p_price DECIMAL(10,2))
BEGIN
    IF p_price < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: price cannot be negative';
    END IF;
    UPDATE products SET price = p_price WHERE product_id = p_product_id;
END$$
DELIMITER ;

-- 23. sp_update_product_status
DELIMITER $$
CREATE PROCEDURE sp_update_product_status(IN p_product_id BIGINT, IN p_status VARCHAR(15))
BEGIN
    UPDATE products SET status = p_status WHERE product_id = p_product_id;
END$$
DELIMITER ;

-- 24. sp_get_products_price_range
DELIMITER $$
CREATE PROCEDURE sp_get_products_price_range(
    IN p_min DECIMAL(10,2), IN p_max DECIMAL(10,2), IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT product_id, sku, name, category_id, price, rating_avg, status
    FROM products
    WHERE status = 'ACTIVE' AND price >= p_min AND price <= p_max
    ORDER BY price
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 25. sp_get_top_rated_products
DELIMITER $$
CREATE PROCEDURE sp_get_top_rated_products(IN p_limit INT)
BEGIN
    SELECT product_id, sku, name, category_id, price, rating_avg, rating_count, status
    FROM products
    WHERE status = 'ACTIVE' AND rating_count > 0
    ORDER BY rating_avg DESC, rating_count DESC
    LIMIT p_limit;
END$$
DELIMITER ;

-- 26. fn_get_product_price
DELIMITER $$
CREATE FUNCTION fn_get_product_price(p_product_id BIGINT)
RETURNS DECIMAL(10,2)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_price DECIMAL(10,2) DEFAULT 0.00;
    SELECT price INTO v_price FROM products WHERE product_id = p_product_id;
    RETURN COALESCE(v_price, 0.00);
END$$
DELIMITER ;

-- 27. fn_discounted_price (scalar: price for a product after a discount code)
DELIMITER $$
CREATE FUNCTION fn_discounted_price(p_product_id BIGINT, p_discount_code VARCHAR(40))
RETURNS DECIMAL(10,2)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_price DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_type VARCHAR(10);
    DECLARE v_value DECIMAL(10,2);
    DECLARE v_max DECIMAL(10,2);
    DECLARE v_status VARCHAR(10);
    DECLARE v_from DATETIME;
    DECLARE v_to DATETIME;
    DECLARE v_discount DECIMAL(10,2) DEFAULT 0.00;

    SELECT price INTO v_price FROM products WHERE product_id = p_product_id;
    IF v_price IS NULL THEN
        RETURN 0.00;
    END IF;

    SELECT discount_type, discount_value, max_discount, status, valid_from, valid_to
    INTO v_type, v_value, v_max, v_status, v_from, v_to
    FROM discount_codes WHERE code = p_discount_code;

    IF v_type IS NULL OR v_status <> 'ACTIVE' OR NOW() < v_from OR NOW() > v_to THEN
        RETURN v_price;
    END IF;

    IF v_type = 'PERCENT' THEN
        SET v_discount = v_price * (v_value / 100);
    ELSE
        SET v_discount = v_value;
    END IF;

    IF v_max IS NOT NULL AND v_discount > v_max THEN
        SET v_discount = v_max;
    END IF;

    IF v_discount > v_price THEN
        SET v_discount = v_price;
    END IF;

    RETURN ROUND(v_price - v_discount, 2);
END$$
DELIMITER ;

-- 28. sp_recalculate_product_rating
DELIMITER $$
CREATE PROCEDURE sp_recalculate_product_rating(IN p_product_id BIGINT)
BEGIN
    DECLARE v_avg DECIMAL(3,2) DEFAULT 0.00;
    DECLARE v_count INT DEFAULT 0;
    SELECT COALESCE(AVG(rating), 0), COUNT(*)
    INTO v_avg, v_count
    FROM reviews WHERE product_id = p_product_id AND status = 'PUBLISHED';
    UPDATE products SET rating_avg = v_avg, rating_count = v_count WHERE product_id = p_product_id;
END$$
DELIMITER ;

-- ============================================================
-- INVENTORY PROCEDURES (29-38)
-- ============================================================

-- 29. sp_set_inventory
DELIMITER $$
CREATE PROCEDURE sp_set_inventory(
    IN p_product_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_quantity INT,
    IN p_reorder_level INT,
    IN p_reorder_qty INT,
    OUT p_inventory_id BIGINT)
BEGIN
    INSERT INTO inventory (product_id, warehouse_id, quantity, reorder_level, reorder_qty)
    VALUES (p_product_id, p_warehouse_id, p_quantity, p_reorder_level, p_reorder_qty)
    ON DUPLICATE KEY UPDATE quantity = p_quantity,
        reorder_level = p_reorder_level, reorder_qty = p_reorder_qty;
    SELECT inventory_id INTO p_inventory_id
    FROM inventory WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id;
END$$
DELIMITER ;

-- 30. sp_get_inventory_by_product
DELIMITER $$
CREATE PROCEDURE sp_get_inventory_by_product(IN p_product_id BIGINT)
BEGIN
    SELECT i.inventory_id, i.product_id, i.warehouse_id, i.quantity, i.reserved,
           i.reorder_level, i.reorder_qty, i.updated_at,
           w.code AS warehouse_code, w.name AS warehouse_name, w.region
    FROM inventory i
    JOIN warehouses w ON i.warehouse_id = w.warehouse_id
    WHERE i.product_id = p_product_id;
END$$
DELIMITER ;

-- 31. sp_get_inventory_by_warehouse
DELIMITER $$
CREATE PROCEDURE sp_get_inventory_by_warehouse(IN p_warehouse_id BIGINT, IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT i.inventory_id, i.product_id, i.warehouse_id, i.quantity, i.reserved,
           i.reorder_level, i.reorder_qty, p.sku, p.name AS product_name
    FROM inventory i
    JOIN products p ON i.product_id = p.product_id
    WHERE i.warehouse_id = p_warehouse_id
    ORDER BY p.name
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 32. sp_adjust_inventory (transactional, can SIGNAL on negative result)
DELIMITER $$
CREATE PROCEDURE sp_adjust_inventory(
    IN p_product_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_delta INT)
BEGIN
    DECLARE v_qty INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT quantity INTO v_qty FROM inventory
    WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id FOR UPDATE;
    IF v_qty IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: inventory record does not exist';
    END IF;
    IF v_qty + p_delta < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK: adjustment would make stock negative';
    END IF;
    UPDATE inventory SET quantity = quantity + p_delta
    WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id;
    COMMIT;
END$$
DELIMITER ;

-- 33. sp_get_low_stock_report
DELIMITER $$
CREATE PROCEDURE sp_get_low_stock_report(IN p_limit INT)
BEGIN
    SELECT i.inventory_id, i.product_id, i.warehouse_id, i.quantity, i.reorder_level,
           i.reorder_qty, p.sku, p.name AS product_name, p.price,
           w.code AS warehouse_code, w.name AS warehouse_name, w.region,
           (i.reorder_level - i.quantity) AS shortfall
    FROM inventory i
    JOIN products p ON i.product_id = p.product_id
    JOIN warehouses w ON i.warehouse_id = w.warehouse_id
    WHERE i.quantity <= i.reorder_level AND p.status = 'ACTIVE'
    ORDER BY shortfall DESC, i.quantity ASC
    LIMIT p_limit;
END$$
DELIMITER ;

-- 34. fn_get_total_stock (scalar: total available across warehouses)
DELIMITER $$
CREATE FUNCTION fn_get_total_stock(p_product_id BIGINT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_total INT DEFAULT 0;
    SELECT COALESCE(SUM(quantity - reserved), 0) INTO v_total
    FROM inventory WHERE product_id = p_product_id;
    RETURN v_total;
END$$
DELIMITER ;

-- 35. fn_is_in_stock
DELIMITER $$
CREATE FUNCTION fn_is_in_stock(p_product_id BIGINT, p_quantity INT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_total INT DEFAULT 0;
    SELECT COALESCE(SUM(quantity - reserved), 0) INTO v_total
    FROM inventory WHERE product_id = p_product_id;
    RETURN IF(v_total >= p_quantity, 1, 0);
END$$
DELIMITER ;

-- 36. sp_reserve_stock (transactional)
DELIMITER $$
CREATE PROCEDURE sp_reserve_stock(
    IN p_product_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_quantity INT)
BEGIN
    DECLARE v_available INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT quantity - reserved INTO v_available FROM inventory
    WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id FOR UPDATE;
    IF v_available IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: inventory record does not exist';
    END IF;
    IF v_available < p_quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK: not enough available stock to reserve';
    END IF;
    UPDATE inventory SET reserved = reserved + p_quantity
    WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id;
    COMMIT;
END$$
DELIMITER ;

-- 37. sp_release_stock
DELIMITER $$
CREATE PROCEDURE sp_release_stock(
    IN p_product_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_quantity INT)
BEGIN
    UPDATE inventory SET reserved = GREATEST(reserved - p_quantity, 0)
    WHERE product_id = p_product_id AND warehouse_id = p_warehouse_id;
END$$
DELIMITER ;

-- 38. sp_get_warehouse_stock_value
DELIMITER $$
CREATE PROCEDURE sp_get_warehouse_stock_value(IN p_warehouse_id BIGINT)
BEGIN
    SELECT w.warehouse_id, w.code, w.name,
           COUNT(i.inventory_id) AS product_count,
           COALESCE(SUM(i.quantity), 0) AS total_units,
           COALESCE(SUM(i.quantity * p.cost), 0.00) AS total_cost_value,
           COALESCE(SUM(i.quantity * p.price), 0.00) AS total_retail_value
    FROM warehouses w
    LEFT JOIN inventory i ON w.warehouse_id = i.warehouse_id
    LEFT JOIN products p ON i.product_id = p.product_id
    WHERE w.warehouse_id = p_warehouse_id
    GROUP BY w.warehouse_id, w.code, w.name;
END$$
DELIMITER ;

-- ============================================================
-- CART PROCEDURES (39-46)
-- ============================================================

-- 39. sp_get_or_create_cart
DELIMITER $$
CREATE PROCEDURE sp_get_or_create_cart(IN p_customer_id BIGINT, OUT p_cart_id BIGINT)
BEGIN
    SELECT cart_id INTO p_cart_id
    FROM carts WHERE customer_id = p_customer_id AND status = 'OPEN'
    ORDER BY created_at DESC LIMIT 1;
    IF p_cart_id IS NULL THEN
        INSERT INTO carts (customer_id, status) VALUES (p_customer_id, 'OPEN');
        SET p_cart_id = LAST_INSERT_ID();
    END IF;
END$$
DELIMITER ;

-- 40. sp_add_to_cart (transactional: validates product + stock)
DELIMITER $$
CREATE PROCEDURE sp_add_to_cart(
    IN p_cart_id BIGINT,
    IN p_product_id BIGINT,
    IN p_quantity INT,
    OUT p_cart_item_id BIGINT)
BEGIN
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_status VARCHAR(15);
    DECLARE v_available INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_quantity <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: quantity must be positive';
    END IF;

    START TRANSACTION;
    SELECT price, status INTO v_price, v_status FROM products WHERE product_id = p_product_id;
    IF v_price IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: product does not exist';
    END IF;
    IF v_status <> 'ACTIVE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: product is not available for purchase';
    END IF;

    SELECT COALESCE(SUM(quantity - reserved), 0) INTO v_available
    FROM inventory WHERE product_id = p_product_id;
    IF v_available < p_quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK: not enough stock for requested quantity';
    END IF;

    INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
    VALUES (p_cart_id, p_product_id, p_quantity, v_price)
    ON DUPLICATE KEY UPDATE quantity = quantity + p_quantity, unit_price = v_price;

    SELECT cart_item_id INTO p_cart_item_id
    FROM cart_items WHERE cart_id = p_cart_id AND product_id = p_product_id;
    COMMIT;
END$$
DELIMITER ;

-- 41. sp_update_cart_item_quantity
DELIMITER $$
CREATE PROCEDURE sp_update_cart_item_quantity(IN p_cart_item_id BIGINT, IN p_quantity INT)
BEGIN
    IF p_quantity <= 0 THEN
        DELETE FROM cart_items WHERE cart_item_id = p_cart_item_id;
    ELSE
        UPDATE cart_items SET quantity = p_quantity WHERE cart_item_id = p_cart_item_id;
    END IF;
END$$
DELIMITER ;

-- 42. sp_remove_cart_item
DELIMITER $$
CREATE PROCEDURE sp_remove_cart_item(IN p_cart_item_id BIGINT)
BEGIN
    DELETE FROM cart_items WHERE cart_item_id = p_cart_item_id;
END$$
DELIMITER ;

-- 43. sp_get_cart_items
DELIMITER $$
CREATE PROCEDURE sp_get_cart_items(IN p_cart_id BIGINT)
BEGIN
    SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.unit_price,
           (ci.quantity * ci.unit_price) AS line_total,
           p.sku, p.name AS product_name, p.status AS product_status
    FROM cart_items ci
    JOIN products p ON ci.product_id = p.product_id
    WHERE ci.cart_id = p_cart_id
    ORDER BY ci.added_at;
END$$
DELIMITER ;

-- 44. fn_get_cart_total
DELIMITER $$
CREATE FUNCTION fn_get_cart_total(p_cart_id BIGINT)
RETURNS DECIMAL(10,2)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;
    SELECT COALESCE(SUM(quantity * unit_price), 0.00) INTO v_total
    FROM cart_items WHERE cart_id = p_cart_id;
    RETURN v_total;
END$$
DELIMITER ;

-- 45. fn_get_cart_item_count
DELIMITER $$
CREATE FUNCTION fn_get_cart_item_count(p_cart_id BIGINT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COALESCE(SUM(quantity), 0) INTO v_count FROM cart_items WHERE cart_id = p_cart_id;
    RETURN v_count;
END$$
DELIMITER ;

-- 46. sp_clear_cart
DELIMITER $$
CREATE PROCEDURE sp_clear_cart(IN p_cart_id BIGINT)
BEGIN
    DELETE FROM cart_items WHERE cart_id = p_cart_id;
END$$
DELIMITER ;

-- ============================================================
-- DISCOUNT PROCEDURES (47-53)
-- ============================================================

-- 47. sp_create_discount_code
DELIMITER $$
CREATE PROCEDURE sp_create_discount_code(
    IN p_code VARCHAR(40),
    IN p_description VARCHAR(300),
    IN p_discount_type VARCHAR(10),
    IN p_discount_value DECIMAL(10,2),
    IN p_min_order_amount DECIMAL(10,2),
    IN p_max_discount DECIMAL(10,2),
    IN p_valid_from DATETIME,
    IN p_valid_to DATETIME,
    IN p_usage_limit INT,
    OUT p_discount_id BIGINT)
BEGIN
    INSERT INTO discount_codes (code, description, discount_type, discount_value,
                                min_order_amount, max_discount, valid_from, valid_to, usage_limit)
    VALUES (p_code, p_description, p_discount_type, p_discount_value,
            COALESCE(p_min_order_amount, 0.00), p_max_discount, p_valid_from, p_valid_to, p_usage_limit);
    SET p_discount_id = LAST_INSERT_ID();
END$$
DELIMITER ;

-- 48. sp_get_discount_by_code
DELIMITER $$
CREATE PROCEDURE sp_get_discount_by_code(IN p_code VARCHAR(40))
BEGIN
    SELECT discount_id, code, description, discount_type, discount_value,
           min_order_amount, max_discount, valid_from, valid_to, usage_limit,
           times_used, status, created_at
    FROM discount_codes WHERE code = p_code;
END$$
DELIMITER ;

-- 49. sp_validate_discount (transactional-safe validation, SIGNAL on invalid)
DELIMITER $$
CREATE PROCEDURE sp_validate_discount(
    IN p_code VARCHAR(40),
    IN p_order_amount DECIMAL(10,2),
    OUT p_discount_amount DECIMAL(10,2))
BEGIN
    DECLARE v_type VARCHAR(10);
    DECLARE v_value DECIMAL(10,2);
    DECLARE v_min DECIMAL(10,2);
    DECLARE v_max DECIMAL(10,2);
    DECLARE v_status VARCHAR(10);
    DECLARE v_from DATETIME;
    DECLARE v_to DATETIME;
    DECLARE v_limit INT;
    DECLARE v_used INT;

    SELECT discount_type, discount_value, min_order_amount, max_discount, status,
           valid_from, valid_to, usage_limit, times_used
    INTO v_type, v_value, v_min, v_max, v_status, v_from, v_to, v_limit, v_used
    FROM discount_codes WHERE code = p_code;

    IF v_type IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount code does not exist';
    END IF;
    IF v_status <> 'ACTIVE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount code is not active';
    END IF;
    IF NOW() < v_from OR NOW() > v_to THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount code is expired or not yet valid';
    END IF;
    IF v_limit IS NOT NULL AND v_used >= v_limit THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount usage limit reached';
    END IF;
    IF p_order_amount < v_min THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: order does not meet minimum amount';
    END IF;

    IF v_type = 'PERCENT' THEN
        SET p_discount_amount = ROUND(p_order_amount * (v_value / 100), 2);
    ELSE
        SET p_discount_amount = v_value;
    END IF;

    IF v_max IS NOT NULL AND p_discount_amount > v_max THEN
        SET p_discount_amount = v_max;
    END IF;
    IF p_discount_amount > p_order_amount THEN
        SET p_discount_amount = p_order_amount;
    END IF;
END$$
DELIMITER ;

-- 50. sp_get_active_discounts
DELIMITER $$
CREATE PROCEDURE sp_get_active_discounts()
BEGIN
    SELECT discount_id, code, description, discount_type, discount_value,
           min_order_amount, max_discount, valid_from, valid_to, usage_limit, times_used
    FROM discount_codes
    WHERE status = 'ACTIVE' AND NOW() BETWEEN valid_from AND valid_to
    ORDER BY valid_to;
END$$
DELIMITER ;

-- 51. sp_disable_discount
DELIMITER $$
CREATE PROCEDURE sp_disable_discount(IN p_discount_id BIGINT)
BEGIN
    UPDATE discount_codes SET status = 'DISABLED' WHERE discount_id = p_discount_id;
END$$
DELIMITER ;

-- 52. sp_expire_discounts (housekeeping)
DELIMITER $$
CREATE PROCEDURE sp_expire_discounts()
BEGIN
    UPDATE discount_codes SET status = 'EXPIRED'
    WHERE status = 'ACTIVE' AND valid_to < NOW();
END$$
DELIMITER ;

-- 53. fn_is_discount_valid
DELIMITER $$
CREATE FUNCTION fn_is_discount_valid(p_code VARCHAR(40))
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count FROM discount_codes
    WHERE code = p_code AND status = 'ACTIVE' AND NOW() BETWEEN valid_from AND valid_to;
    RETURN v_count;
END$$
DELIMITER ;

-- ============================================================
-- ORDER PROCEDURES (54-70) - includes transactional sp_place_order
-- ============================================================

-- 54. sp_place_order (THE core transactional procedure)
-- Atomically: validates cart, checks + decrements inventory for each line,
-- creates the order and line items, applies a discount if provided, converts
-- the cart. Raises SIGNAL on insufficient stock or invalid discount; the whole
-- thing rolls back on any failure.
DELIMITER $$
CREATE PROCEDURE sp_place_order(
    IN p_customer_id BIGINT,
    IN p_cart_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_discount_code VARCHAR(40),
    IN p_shipping_address VARCHAR(400),
    IN p_shipping_amount DECIMAL(10,2),
    OUT p_order_id BIGINT)
BEGIN
    DECLARE v_subtotal DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_discount_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_discount_id BIGINT DEFAULT NULL;
    DECLARE v_tax DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_order_ref VARCHAR(30);
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE v_cust_status VARCHAR(10);
    -- discount vars
    DECLARE v_d_type VARCHAR(10);
    DECLARE v_d_value DECIMAL(10,2);
    DECLARE v_d_min DECIMAL(10,2);
    DECLARE v_d_max DECIMAL(10,2);
    DECLARE v_d_status VARCHAR(10);
    DECLARE v_d_from DATETIME;
    DECLARE v_d_to DATETIME;
    DECLARE v_d_limit INT;
    DECLARE v_d_used INT;
    -- cursor over cart items
    DECLARE v_done INT DEFAULT 0;
    DECLARE c_product_id BIGINT;
    DECLARE c_quantity INT;
    DECLARE c_unit_price DECIMAL(10,2);
    DECLARE c_sku VARCHAR(40);
    DECLARE c_name VARCHAR(200);
    DECLARE v_available INT;
    DECLARE v_dec_remaining INT;
    DECLARE v_wh_qty INT;
    DECLARE v_wh_id BIGINT;

    DECLARE cur CURSOR FOR
        SELECT ci.product_id, ci.quantity, ci.unit_price, p.sku, p.name
        FROM cart_items ci JOIN products p ON ci.product_id = p.product_id
        WHERE ci.cart_id = p_cart_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- validate customer
    SELECT status INTO v_cust_status FROM customers WHERE customer_id = p_customer_id FOR UPDATE;
    IF v_cust_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: customer does not exist';
    END IF;
    IF v_cust_status <> 'ACTIVE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: customer is not active';
    END IF;

    -- cart must have items
    SELECT COUNT(*) INTO v_item_count FROM cart_items WHERE cart_id = p_cart_id;
    IF v_item_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: cart is empty';
    END IF;

    -- compute subtotal
    SELECT COALESCE(SUM(quantity * unit_price), 0.00) INTO v_subtotal
    FROM cart_items WHERE cart_id = p_cart_id;

    -- apply discount if provided
    IF p_discount_code IS NOT NULL AND p_discount_code <> '' THEN
        SELECT discount_id, discount_type, discount_value, min_order_amount, max_discount,
               status, valid_from, valid_to, usage_limit, times_used
        INTO v_discount_id, v_d_type, v_d_value, v_d_min, v_d_max,
             v_d_status, v_d_from, v_d_to, v_d_limit, v_d_used
        FROM discount_codes WHERE code = p_discount_code FOR UPDATE;

        IF v_discount_id IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount code does not exist';
        END IF;
        IF v_d_status <> 'ACTIVE' OR NOW() < v_d_from OR NOW() > v_d_to THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount code is expired or inactive';
        END IF;
        IF v_d_limit IS NOT NULL AND v_d_used >= v_d_limit THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: discount usage limit reached';
        END IF;
        IF v_subtotal < v_d_min THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DISCOUNT_INVALID: order below minimum for this discount';
        END IF;

        IF v_d_type = 'PERCENT' THEN
            SET v_discount_amount = ROUND(v_subtotal * (v_d_value / 100), 2);
        ELSE
            SET v_discount_amount = v_d_value;
        END IF;
        IF v_d_max IS NOT NULL AND v_discount_amount > v_d_max THEN
            SET v_discount_amount = v_d_max;
        END IF;
        IF v_discount_amount > v_subtotal THEN
            SET v_discount_amount = v_subtotal;
        END IF;
    END IF;

    -- tax 8% on discounted subtotal; total
    SET v_tax = ROUND((v_subtotal - v_discount_amount) * 0.08, 2);
    SET v_total = (v_subtotal - v_discount_amount) + v_tax + COALESCE(p_shipping_amount, 0.00);
    SET v_order_ref = CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND()*1000000), 6, '0'));

    -- create order
    INSERT INTO orders (order_ref, customer_id, warehouse_id, discount_id, status,
                        subtotal, discount_amount, tax_amount, shipping_amount, total_amount, shipping_address)
    VALUES (v_order_ref, p_customer_id, p_warehouse_id, v_discount_id, 'PLACED',
            v_subtotal, v_discount_amount, v_tax, COALESCE(p_shipping_amount, 0.00), v_total, p_shipping_address);
    SET p_order_id = LAST_INSERT_ID();

    -- iterate cart items: check stock, create line item, decrement inventory
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO c_product_id, c_quantity, c_unit_price, c_sku, c_name;
        IF v_done = 1 THEN
            LEAVE read_loop;
        END IF;

        SELECT COALESCE(SUM(quantity - reserved), 0) INTO v_available
        FROM inventory WHERE product_id = c_product_id;
        IF v_available < c_quantity THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK: not enough stock to fulfil order line';
        END IF;

        INSERT INTO order_items (order_id, product_id, sku, product_name, quantity, unit_price, line_total)
        VALUES (p_order_id, c_product_id, c_sku, c_name, c_quantity, c_unit_price,
                ROUND(c_quantity * c_unit_price, 2));

        -- decrement inventory across warehouses (greedy), preferring the chosen warehouse
        SET v_dec_remaining = c_quantity;

        IF p_warehouse_id IS NOT NULL THEN
            SELECT quantity INTO v_wh_qty FROM inventory
            WHERE product_id = c_product_id AND warehouse_id = p_warehouse_id FOR UPDATE;
            IF v_wh_qty IS NOT NULL AND v_wh_qty > 0 THEN
                IF v_wh_qty >= v_dec_remaining THEN
                    UPDATE inventory SET quantity = quantity - v_dec_remaining
                    WHERE product_id = c_product_id AND warehouse_id = p_warehouse_id;
                    SET v_dec_remaining = 0;
                ELSE
                    UPDATE inventory SET quantity = 0
                    WHERE product_id = c_product_id AND warehouse_id = p_warehouse_id;
                    SET v_dec_remaining = v_dec_remaining - v_wh_qty;
                END IF;
            END IF;
        END IF;

        -- decrement from any other warehouses with stock until satisfied
        WHILE v_dec_remaining > 0 DO
            SET v_wh_id = NULL;
            SELECT warehouse_id, quantity INTO v_wh_id, v_wh_qty
            FROM inventory
            WHERE product_id = c_product_id AND quantity > 0
            ORDER BY quantity DESC LIMIT 1 FOR UPDATE;
            IF v_wh_id IS NULL THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK: inventory exhausted during fulfilment';
            END IF;
            IF v_wh_qty >= v_dec_remaining THEN
                UPDATE inventory SET quantity = quantity - v_dec_remaining WHERE product_id = c_product_id AND warehouse_id = v_wh_id;
                SET v_dec_remaining = 0;
            ELSE
                UPDATE inventory SET quantity = 0 WHERE product_id = c_product_id AND warehouse_id = v_wh_id;
                SET v_dec_remaining = v_dec_remaining - v_wh_qty;
            END IF;
        END WHILE;
    END LOOP;
    CLOSE cur;

    -- increment discount usage
    IF v_discount_id IS NOT NULL THEN
        UPDATE discount_codes SET times_used = times_used + 1 WHERE discount_id = v_discount_id;
    END IF;

    -- convert cart
    UPDATE carts SET status = 'CONVERTED' WHERE cart_id = p_cart_id;

    -- status history
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, NULL, 'PLACED', 'SYSTEM', 'Order placed');

    COMMIT;
END$$
DELIMITER ;

-- 55. sp_get_order_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_order_by_id(IN p_order_id BIGINT)
BEGIN
    SELECT o.order_id, o.order_ref, o.customer_id, o.warehouse_id, o.discount_id, o.status,
           o.subtotal, o.discount_amount, o.tax_amount, o.shipping_amount, o.total_amount,
           o.currency, o.shipping_address, o.placed_at, o.updated_at,
           c.first_name, c.last_name, c.email
    FROM orders o
    JOIN customers c ON o.customer_id = c.customer_id
    WHERE o.order_id = p_order_id;
END$$
DELIMITER ;

-- 56. sp_get_order_by_ref
DELIMITER $$
CREATE PROCEDURE sp_get_order_by_ref(IN p_order_ref VARCHAR(30))
BEGIN
    SELECT order_id, order_ref, customer_id, warehouse_id, discount_id, status,
           subtotal, discount_amount, tax_amount, shipping_amount, total_amount,
           currency, shipping_address, placed_at, updated_at
    FROM orders WHERE order_ref = p_order_ref;
END$$
DELIMITER ;

-- 57. sp_get_order_items
DELIMITER $$
CREATE PROCEDURE sp_get_order_items(IN p_order_id BIGINT)
BEGIN
    SELECT order_item_id, order_id, product_id, sku, product_name, quantity,
           unit_price, line_total, created_at
    FROM order_items WHERE order_id = p_order_id ORDER BY order_item_id;
END$$
DELIMITER ;

-- 58. sp_get_orders_by_customer
DELIMITER $$
CREATE PROCEDURE sp_get_orders_by_customer(IN p_customer_id BIGINT, IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT order_id, order_ref, customer_id, status, subtotal, discount_amount,
           tax_amount, shipping_amount, total_amount, currency, placed_at
    FROM orders WHERE customer_id = p_customer_id
    ORDER BY placed_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 59. sp_get_orders_by_status
DELIMITER $$
CREATE PROCEDURE sp_get_orders_by_status(IN p_status VARCHAR(15), IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT order_id, order_ref, customer_id, status, total_amount, currency, placed_at
    FROM orders WHERE status = p_status
    ORDER BY placed_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 60. sp_update_order_status (transactional with valid-transition SIGNAL)
DELIMITER $$
CREATE PROCEDURE sp_update_order_status(
    IN p_order_id BIGINT,
    IN p_new_status VARCHAR(15),
    IN p_changed_by VARCHAR(80),
    IN p_note VARCHAR(300))
BEGIN
    DECLARE v_old VARCHAR(15);
    DECLARE v_ok INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT status INTO v_old FROM orders WHERE order_id = p_order_id FOR UPDATE;
    IF v_old IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: order does not exist';
    END IF;

    -- allowed transitions
    SET v_ok = CASE
        WHEN v_old = 'PLACED' AND p_new_status IN ('PAID','CANCELLED') THEN 1
        WHEN v_old = 'PAID' AND p_new_status IN ('SHIPPED','CANCELLED','REFUNDED') THEN 1
        WHEN v_old = 'SHIPPED' AND p_new_status IN ('DELIVERED','RETURNED') THEN 1
        WHEN v_old = 'DELIVERED' AND p_new_status IN ('RETURNED') THEN 1
        WHEN v_old = 'RETURNED' AND p_new_status IN ('REFUNDED') THEN 1
        ELSE 0 END;

    IF v_ok = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: illegal order status transition';
    END IF;

    UPDATE orders SET status = p_new_status WHERE order_id = p_order_id;
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, v_old, p_new_status, COALESCE(p_changed_by, 'SYSTEM'), p_note);
    COMMIT;
END$$
DELIMITER ;

-- 61. sp_cancel_order (transactional: restore inventory)
DELIMITER $$
CREATE PROCEDURE sp_cancel_order(IN p_order_id BIGINT, IN p_reason VARCHAR(300))
BEGIN
    DECLARE v_status VARCHAR(15);
    DECLARE v_wh BIGINT;
    DECLARE v_done INT DEFAULT 0;
    DECLARE c_product_id BIGINT;
    DECLARE c_quantity INT;
    DECLARE cur CURSOR FOR SELECT product_id, quantity FROM order_items WHERE order_id = p_order_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT status, warehouse_id INTO v_status, v_wh FROM orders WHERE order_id = p_order_id FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: order does not exist';
    END IF;
    IF v_status NOT IN ('PLACED','PAID') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: only PLACED or PAID orders can be cancelled';
    END IF;

    OPEN cur;
    restore_loop: LOOP
        FETCH cur INTO c_product_id, c_quantity;
        IF v_done = 1 THEN LEAVE restore_loop; END IF;
        IF v_wh IS NOT NULL THEN
            UPDATE inventory SET quantity = quantity + c_quantity
            WHERE product_id = c_product_id AND warehouse_id = v_wh;
        ELSE
            UPDATE inventory SET quantity = quantity + c_quantity
            WHERE product_id = c_product_id
            ORDER BY warehouse_id LIMIT 1;
        END IF;
    END LOOP;
    CLOSE cur;

    UPDATE orders SET status = 'CANCELLED' WHERE order_id = p_order_id;
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, v_status, 'CANCELLED', 'SYSTEM', COALESCE(p_reason, 'Order cancelled'));
    COMMIT;
END$$
DELIMITER ;

-- 62. sp_get_order_status_history
DELIMITER $$
CREATE PROCEDURE sp_get_order_status_history(IN p_order_id BIGINT)
BEGIN
    SELECT history_id, order_id, old_status, new_status, changed_by, note, created_at
    FROM order_status_history WHERE order_id = p_order_id ORDER BY created_at;
END$$
DELIMITER ;

-- 63. fn_get_order_item_count
DELIMITER $$
CREATE FUNCTION fn_get_order_item_count(p_order_id BIGINT)
RETURNS INT
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COALESCE(SUM(quantity), 0) INTO v_count FROM order_items WHERE order_id = p_order_id;
    RETURN v_count;
END$$
DELIMITER ;

-- 64. fn_get_order_status
DELIMITER $$
CREATE FUNCTION fn_get_order_status(p_order_id BIGINT)
RETURNS VARCHAR(15)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_status VARCHAR(15);
    SELECT status INTO v_status FROM orders WHERE order_id = p_order_id;
    RETURN COALESCE(v_status, 'UNKNOWN');
END$$
DELIMITER ;

-- 65. sp_get_recent_orders
DELIMITER $$
CREATE PROCEDURE sp_get_recent_orders(IN p_limit INT)
BEGIN
    SELECT o.order_id, o.order_ref, o.customer_id, o.status, o.total_amount, o.placed_at,
           c.first_name, c.last_name
    FROM orders o JOIN customers c ON o.customer_id = c.customer_id
    ORDER BY o.placed_at DESC
    LIMIT p_limit;
END$$
DELIMITER ;

-- 66. sp_get_orders_date_range
DELIMITER $$
CREATE PROCEDURE sp_get_orders_date_range(IN p_from DATETIME, IN p_to DATETIME, IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT order_id, order_ref, customer_id, status, total_amount, placed_at
    FROM orders
    WHERE placed_at >= p_from AND placed_at <= p_to
    ORDER BY placed_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 67. sp_get_order_full (order + items in one call via two result sets is not used;
--     returns a joined summary row with computed counts)
DELIMITER $$
CREATE PROCEDURE sp_get_order_full(IN p_order_id BIGINT)
BEGIN
    SELECT o.order_id, o.order_ref, o.customer_id, o.status, o.subtotal, o.discount_amount,
           o.tax_amount, o.shipping_amount, o.total_amount, o.currency, o.shipping_address,
           o.placed_at, c.first_name, c.last_name, c.email,
           fn_get_order_item_count(o.order_id) AS item_count
    FROM orders o JOIN customers c ON o.customer_id = c.customer_id
    WHERE o.order_id = p_order_id;
END$$
DELIMITER ;

-- 68. sp_count_orders_by_status
DELIMITER $$
CREATE PROCEDURE sp_count_orders_by_status()
BEGIN
    SELECT status, COUNT(*) AS order_count, COALESCE(SUM(total_amount),0.00) AS total_value
    FROM orders GROUP BY status;
END$$
DELIMITER ;

-- 69. sp_get_top_customers_by_spend
DELIMITER $$
CREATE PROCEDURE sp_get_top_customers_by_spend(IN p_limit INT)
BEGIN
    SELECT c.customer_id, c.first_name, c.last_name, c.email, c.loyalty_tier,
           COUNT(o.order_id) AS order_count,
           COALESCE(SUM(o.total_amount), 0.00) AS total_spend
    FROM customers c
    JOIN orders o ON c.customer_id = o.customer_id
    WHERE o.status IN ('PAID','SHIPPED','DELIVERED')
    GROUP BY c.customer_id, c.first_name, c.last_name, c.email, c.loyalty_tier
    ORDER BY total_spend DESC
    LIMIT p_limit;
END$$
DELIMITER ;

-- 70. sp_get_customer_order_summary (OUT params)
DELIMITER $$
CREATE PROCEDURE sp_get_customer_order_summary(
    IN p_customer_id BIGINT,
    OUT p_order_count INT,
    OUT p_total_spend DECIMAL(12,2))
BEGIN
    SELECT COUNT(*), COALESCE(SUM(total_amount), 0.00)
    INTO p_order_count, p_total_spend
    FROM orders
    WHERE customer_id = p_customer_id AND status IN ('PAID','SHIPPED','DELIVERED');
END$$
DELIMITER ;

-- ============================================================
-- PAYMENT PROCEDURES (71-76)
-- ============================================================

-- 71. sp_process_payment (transactional: records payment, advances order to PAID)
DELIMITER $$
CREATE PROCEDURE sp_process_payment(
    IN p_order_id BIGINT,
    IN p_amount DECIMAL(10,2),
    IN p_method VARCHAR(20),
    IN p_transaction_id VARCHAR(60),
    OUT p_payment_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(15);
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_ref VARCHAR(30);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_amount <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: payment amount must be positive';
    END IF;

    START TRANSACTION;
    SELECT status, total_amount INTO v_status, v_total FROM orders WHERE order_id = p_order_id FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: order does not exist';
    END IF;
    IF v_status <> 'PLACED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: order is not awaiting payment';
    END IF;
    IF p_amount < v_total THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'PAYMENT_ERROR: payment amount is less than the order total';
    END IF;

    SET v_ref = CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND()*1000000), 6, '0'));
    INSERT INTO payments (payment_ref, order_id, amount, method, status, transaction_id)
    VALUES (v_ref, p_order_id, p_amount, p_method, 'CAPTURED', p_transaction_id);
    SET p_payment_id = LAST_INSERT_ID();

    UPDATE orders SET status = 'PAID' WHERE order_id = p_order_id;
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, 'PLACED', 'PAID', 'SYSTEM', CONCAT('Payment captured: ', v_ref));
    COMMIT;
END$$
DELIMITER ;

-- 72. sp_get_payment_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_payment_by_id(IN p_payment_id BIGINT)
BEGIN
    SELECT payment_id, payment_ref, order_id, amount, currency, method, status,
           transaction_id, created_at, updated_at
    FROM payments WHERE payment_id = p_payment_id;
END$$
DELIMITER ;

-- 73. sp_get_payments_by_order
DELIMITER $$
CREATE PROCEDURE sp_get_payments_by_order(IN p_order_id BIGINT)
BEGIN
    SELECT payment_id, payment_ref, order_id, amount, currency, method, status,
           transaction_id, created_at
    FROM payments WHERE order_id = p_order_id ORDER BY created_at;
END$$
DELIMITER ;

-- 74. sp_refund_payment (transactional)
DELIMITER $$
CREATE PROCEDURE sp_refund_payment(IN p_payment_id BIGINT, OUT p_result INT)
BEGIN
    DECLARE v_status VARCHAR(15);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT status INTO v_status FROM payments WHERE payment_id = p_payment_id FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: payment does not exist';
    END IF;
    IF v_status <> 'CAPTURED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'PAYMENT_ERROR: only captured payments can be refunded';
    END IF;
    UPDATE payments SET status = 'REFUNDED' WHERE payment_id = p_payment_id;
    SET p_result = 1;
    COMMIT;
END$$
DELIMITER ;

-- 75. fn_get_order_paid_amount
DELIMITER $$
CREATE FUNCTION fn_get_order_paid_amount(p_order_id BIGINT)
RETURNS DECIMAL(10,2)
DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;
    SELECT COALESCE(SUM(amount), 0.00) INTO v_total
    FROM payments WHERE order_id = p_order_id AND status = 'CAPTURED';
    RETURN v_total;
END$$
DELIMITER ;

-- 76. sp_get_payments_by_status
DELIMITER $$
CREATE PROCEDURE sp_get_payments_by_status(IN p_status VARCHAR(15), IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT payment_id, payment_ref, order_id, amount, method, status, created_at
    FROM payments WHERE status = p_status
    ORDER BY created_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- ============================================================
-- SHIPMENT PROCEDURES (77-81)
-- ============================================================

-- 77. sp_create_shipment (transactional: advances order to SHIPPED)
DELIMITER $$
CREATE PROCEDURE sp_create_shipment(
    IN p_order_id BIGINT,
    IN p_warehouse_id BIGINT,
    IN p_carrier VARCHAR(80),
    IN p_tracking_number VARCHAR(80),
    IN p_shipping_cost DECIMAL(10,2),
    OUT p_shipment_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(15);
    DECLARE v_ref VARCHAR(30);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT status INTO v_status FROM orders WHERE order_id = p_order_id FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: order does not exist';
    END IF;
    IF v_status <> 'PAID' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: only PAID orders can be shipped';
    END IF;

    SET v_ref = CONCAT('SHP', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND()*1000000), 6, '0'));
    INSERT INTO shipments (shipment_ref, order_id, warehouse_id, carrier, tracking_number,
                           status, shipping_cost, shipped_at)
    VALUES (v_ref, p_order_id, p_warehouse_id, p_carrier, p_tracking_number,
            'DISPATCHED', COALESCE(p_shipping_cost, 0.00), NOW());
    SET p_shipment_id = LAST_INSERT_ID();

    UPDATE orders SET status = 'SHIPPED' WHERE order_id = p_order_id;
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, 'PAID', 'SHIPPED', 'SYSTEM', CONCAT('Shipment created: ', v_ref));
    COMMIT;
END$$
DELIMITER ;

-- 78. sp_get_shipment_by_id
DELIMITER $$
CREATE PROCEDURE sp_get_shipment_by_id(IN p_shipment_id BIGINT)
BEGIN
    SELECT shipment_id, shipment_ref, order_id, warehouse_id, carrier, tracking_number,
           status, shipping_cost, shipped_at, delivered_at, created_at
    FROM shipments WHERE shipment_id = p_shipment_id;
END$$
DELIMITER ;

-- 79. sp_get_shipments_by_order
DELIMITER $$
CREATE PROCEDURE sp_get_shipments_by_order(IN p_order_id BIGINT)
BEGIN
    SELECT shipment_id, shipment_ref, order_id, warehouse_id, carrier, tracking_number,
           status, shipping_cost, shipped_at, delivered_at, created_at
    FROM shipments WHERE order_id = p_order_id ORDER BY created_at;
END$$
DELIMITER ;

-- 80. sp_mark_shipment_delivered (transactional: advances order to DELIVERED)
DELIMITER $$
CREATE PROCEDURE sp_mark_shipment_delivered(IN p_shipment_id BIGINT)
BEGIN
    DECLARE v_order_id BIGINT;
    DECLARE v_order_status VARCHAR(15);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT order_id INTO v_order_id FROM shipments WHERE shipment_id = p_shipment_id FOR UPDATE;
    IF v_order_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: shipment does not exist';
    END IF;

    UPDATE shipments SET status = 'DELIVERED', delivered_at = NOW() WHERE shipment_id = p_shipment_id;

    SELECT status INTO v_order_status FROM orders WHERE order_id = v_order_id FOR UPDATE;
    IF v_order_status = 'SHIPPED' THEN
        UPDATE orders SET status = 'DELIVERED' WHERE order_id = v_order_id;
        INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
        VALUES (v_order_id, 'SHIPPED', 'DELIVERED', 'SYSTEM', 'Shipment delivered');
    END IF;
    COMMIT;
END$$
DELIMITER ;

-- 81. sp_update_shipment_status
DELIMITER $$
CREATE PROCEDURE sp_update_shipment_status(IN p_shipment_id BIGINT, IN p_status VARCHAR(15))
BEGIN
    UPDATE shipments SET status = p_status WHERE shipment_id = p_shipment_id;
END$$
DELIMITER ;

-- ============================================================
-- RETURN / REVIEW / REPORT PROCEDURES (82-85)
-- ============================================================

-- 82. sp_create_return (transactional: validates order is delivered, advances to RETURNED)
DELIMITER $$
CREATE PROCEDURE sp_create_return(
    IN p_order_id BIGINT,
    IN p_reason VARCHAR(300),
    IN p_refund_amount DECIMAL(10,2),
    OUT p_return_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(15);
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_ref VARCHAR(30);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT status, total_amount INTO v_status, v_total FROM orders WHERE order_id = p_order_id FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: order does not exist';
    END IF;
    IF v_status NOT IN ('DELIVERED','SHIPPED') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_STATUS: only delivered/shipped orders can be returned';
    END IF;
    IF p_refund_amount > v_total THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: refund amount exceeds order total';
    END IF;

    SET v_ref = CONCAT('RET', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND()*1000000), 6, '0'));
    INSERT INTO returns (return_ref, order_id, reason, refund_amount, status)
    VALUES (v_ref, p_order_id, p_reason, COALESCE(p_refund_amount, 0.00), 'REQUESTED');
    SET p_return_id = LAST_INSERT_ID();

    UPDATE orders SET status = 'RETURNED' WHERE order_id = p_order_id;
    INSERT INTO order_status_history (order_id, old_status, new_status, changed_by, note)
    VALUES (p_order_id, v_status, 'RETURNED', 'SYSTEM', COALESCE(p_reason, 'Return requested'));
    COMMIT;
END$$
DELIMITER ;

-- 83. sp_submit_review (transactional: insert review, recalc product rating)
DELIMITER $$
CREATE PROCEDURE sp_submit_review(
    IN p_product_id BIGINT,
    IN p_customer_id BIGINT,
    IN p_rating TINYINT,
    IN p_title VARCHAR(200),
    IN p_body TEXT,
    OUT p_review_id BIGINT)
BEGIN
    DECLARE v_avg DECIMAL(3,2) DEFAULT 0.00;
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_verified INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_rating < 1 OR p_rating > 5 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT: rating must be between 1 and 5';
    END IF;

    START TRANSACTION;
    IF fn_get_product_price(p_product_id) = 0.00
       AND (SELECT COUNT(*) FROM products WHERE product_id = p_product_id) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_FOUND: product does not exist';
    END IF;

    -- mark verified if the customer has a delivered order containing this product
    SELECT COUNT(*) INTO v_verified
    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.customer_id = p_customer_id AND oi.product_id = p_product_id
      AND o.status IN ('DELIVERED','RETURNED','REFUNDED');

    INSERT INTO reviews (product_id, customer_id, rating, title, body, verified_purchase)
    VALUES (p_product_id, p_customer_id, p_rating, p_title, p_body, IF(v_verified > 0, 1, 0))
    ON DUPLICATE KEY UPDATE rating = p_rating, title = p_title, body = p_body;
    SET p_review_id = LAST_INSERT_ID();

    SELECT COALESCE(AVG(rating), 0), COUNT(*) INTO v_avg, v_count
    FROM reviews WHERE product_id = p_product_id AND status = 'PUBLISHED';
    UPDATE products SET rating_avg = v_avg, rating_count = v_count WHERE product_id = p_product_id;
    COMMIT;
END$$
DELIMITER ;

-- 84. sp_get_reviews_by_product
DELIMITER $$
CREATE PROCEDURE sp_get_reviews_by_product(IN p_product_id BIGINT, IN p_limit INT, IN p_offset INT)
BEGIN
    SELECT r.review_id, r.product_id, r.customer_id, r.rating, r.title, r.body,
           r.verified_purchase, r.status, r.created_at,
           c.first_name, c.last_name
    FROM reviews r
    JOIN customers c ON r.customer_id = c.customer_id
    WHERE r.product_id = p_product_id AND r.status = 'PUBLISHED'
    ORDER BY r.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$
DELIMITER ;

-- 85. sp_get_sales_report (aggregate sales by category)
DELIMITER $$
CREATE PROCEDURE sp_get_sales_report(IN p_from DATETIME, IN p_to DATETIME)
BEGIN
    SELECT cat.category_id, cat.name AS category_name,
           COUNT(DISTINCT o.order_id) AS order_count,
           COALESCE(SUM(oi.quantity), 0) AS units_sold,
           COALESCE(SUM(oi.line_total), 0.00) AS gross_revenue
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.order_id
    JOIN products p ON oi.product_id = p.product_id
    JOIN categories cat ON p.category_id = cat.category_id
    WHERE o.status IN ('PAID','SHIPPED','DELIVERED')
      AND o.placed_at >= p_from AND o.placed_at <= p_to
    GROUP BY cat.category_id, cat.name
    ORDER BY gross_revenue DESC;
END$$
DELIMITER ;
