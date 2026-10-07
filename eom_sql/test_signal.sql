USE ecommerce_order;
-- expired discount via sp_validate_discount
CALL sp_validate_discount('EXPIRED5', 100.00, @d);
