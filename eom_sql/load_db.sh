#!/bin/bash
set -e
DBPASS='SocialPlatform_2026!'
SQLDIR=/home/ec2-user/eom_sql

echo "=== Loading schema.sql ==="
mysql --no-defaults -u root -p"$DBPASS" < "$SQLDIR/schema.sql"
echo "SCHEMA_OK"

echo "=== Loading procedures.sql ==="
mysql --no-defaults -u root -p"$DBPASS" < "$SQLDIR/procedures.sql"
echo "PROCEDURES_OK"

echo "=== Loading data.sql ==="
mysql --no-defaults -u root -p"$DBPASS" < "$SQLDIR/data.sql"
echo "DATA_OK"

echo "=== Verification ==="
mysql --no-defaults -u root -p"$DBPASS" ecommerce_order -e "
SELECT COUNT(*) AS tables_count FROM information_schema.tables WHERE table_schema='ecommerce_order';
SELECT routine_type, COUNT(*) AS cnt FROM information_schema.routines WHERE routine_schema='ecommerce_order' GROUP BY routine_type;
SELECT COUNT(*) AS total_routines FROM information_schema.routines WHERE routine_schema='ecommerce_order';
SELECT COUNT(*) AS categories FROM categories;
SELECT COUNT(*) AS warehouses FROM warehouses;
SELECT COUNT(*) AS customers FROM customers;
SELECT COUNT(*) AS products FROM products;
SELECT COUNT(*) AS inventory_rows FROM inventory;
SELECT COUNT(*) AS low_stock FROM inventory WHERE quantity <= reorder_level;
SELECT COUNT(*) AS orders FROM orders;
SELECT COUNT(*) AS order_items FROM order_items;
SELECT COUNT(*) AS payments FROM payments;
SELECT COUNT(*) AS shipments FROM shipments;
SELECT COUNT(*) AS reviews FROM reviews;
SELECT COUNT(*) AS discount_codes FROM discount_codes;
"
echo "ALL_LOADED"
