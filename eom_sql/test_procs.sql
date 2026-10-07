USE ecommerce_order;

-- Test 1: place an order from cart 1 (customer 1) with discount WELCOME10
SELECT '--- TEST 1: place order (cart 1, customer 1) ---' AS test;
SELECT product_id, quantity FROM inventory i WHERE product_id IN (1,2) ORDER BY product_id;
SELECT fn_get_total_stock(1) AS stock_p1_before, fn_get_total_stock(2) AS stock_p2_before;
CALL sp_place_order(1, 1, 1, 'WELCOME10', '12 Oak St', 9.99, @oid);
SELECT @oid AS new_order_id;
SELECT order_id, status, subtotal, discount_amount, tax_amount, shipping_amount, total_amount FROM orders WHERE order_id = @oid;
SELECT fn_get_total_stock(1) AS stock_p1_after, fn_get_total_stock(2) AS stock_p2_after;
SELECT order_item_id, product_id, quantity, unit_price, line_total FROM order_items WHERE order_id = @oid;

-- Test 2: process payment
SELECT '--- TEST 2: process payment ---' AS test;
SELECT total_amount INTO @tot FROM orders WHERE order_id = @oid;
CALL sp_process_payment(@oid, @tot, 'CARD', 'TXNTEST001', @pid);
SELECT @pid AS payment_id;
SELECT status FROM orders WHERE order_id = @oid;

-- Test 3: create shipment
SELECT '--- TEST 3: create shipment ---' AS test;
CALL sp_create_shipment(@oid, 1, 'FedEx', 'TRK123', 9.99, @sid);
SELECT @sid AS shipment_id;
SELECT status FROM orders WHERE order_id = @oid;

-- Test 4: mark delivered
SELECT '--- TEST 4: deliver ---' AS test;
CALL sp_mark_shipment_delivered(@sid);
SELECT status FROM orders WHERE order_id = @oid;

-- Test 5: scalar functions
SELECT '--- TEST 5: scalar functions ---' AS test;
SELECT fn_discounted_price(1, 'WELCOME10') AS discounted_p1;
SELECT fn_get_product_price(1) AS raw_p1;
SELECT fn_is_in_stock(1, 2) AS in_stock;
SELECT fn_is_discount_valid('WELCOME10') AS welcome_valid, fn_is_discount_valid('EXPIRED5') AS expired_valid;

-- Test 6: low stock report
SELECT '--- TEST 6: low stock report ---' AS test;
CALL sp_get_low_stock_report(5);
