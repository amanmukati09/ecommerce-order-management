# Database Reference — ecommerce_order

MySQL 8.x schema, stored-procedure catalog, and seed data for Ecommerce Order
Management.

## Monetary Data Integrity

Every price/money column is declared `DECIMAL(10,2) NOT NULL` (with sensible
defaults where appropriate). Weight uses `DECIMAL(8,3)`, product rating uses
`DECIMAL(3,2)`. No monetary value is stored as a floating-point type.

## Schema Overview (15 tables)

```
categories ─< products ─< inventory >── warehouses
                       ├─< order_items >── orders
                       ├─< cart_items  >── carts >── customers
                       └─< reviews     >── customers
orders ─┬─< payments
        ├─< shipments >── warehouses
        ├─< returns
        └─< order_status_history
discount_codes ─< orders
```

### Table Reference

| Table                | Purpose                                           | Key money columns |
|----------------------|---------------------------------------------------|-------------------|
| categories           | Product categories (self-referential parent_id)   | —                 |
| customers            | Shoppers with loyalty tier + status               | —                 |
| warehouses           | Fulfilment centres by region                      | —                 |
| products             | Catalog items                                     | price, cost       |
| inventory            | Stock per product/warehouse (+ reserved, reorder) | —                 |
| discount_codes       | Percent/fixed discounts with validity + limits    | discount_value, min_order_amount, max_discount |
| carts                | Shopping carts (OPEN/CONVERTED/ABANDONED)         | —                 |
| cart_items           | Lines in a cart                                   | unit_price        |
| orders               | Orders with status lifecycle                      | subtotal, discount_amount, tax_amount, shipping_amount, total_amount |
| order_items          | Order line items (snapshotted sku/name/price)     | unit_price, line_total |
| payments             | Payment records                                   | amount            |
| shipments            | Shipment records with carrier/tracking            | shipping_cost     |
| returns              | Return/refund records                             | refund_amount     |
| reviews              | Product reviews (1 per product/customer)          | —                 |
| order_status_history | Audit trail of order status transitions           | —                 |

Indexes cover all foreign keys and the hot query paths (e.g.
`idx_orders_customer_placed (customer_id, placed_at DESC)`,
`idx_products_category`, `idx_products_price`,
`idx_inventory_low (quantity, reorder_level)`).

## Order Status Lifecycle

```
PLACED ──pay──> PAID ──ship──> SHIPPED ──deliver──> DELIVERED
   │              │                │                    │
   └─cancel─> CANCELLED            └─> RETURNED <────────┘
                  PAID ──> REFUNDED          RETURNED ──> REFUNDED
```

`sp_update_order_status` enforces the allowed transitions with a `CASE` ladder
and raises `INVALID_STATUS` via `SIGNAL` for any illegal transition.

## Transactional Procedures

These perform multi-step mutations atomically inside
`START TRANSACTION ... COMMIT` with
`DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END`:

- `sp_place_order` — the core flow: validate, price, discount, create order +
  items, check + decrement inventory (cursor over cart items, greedy across
  warehouses), bump discount usage, convert cart, write history
- `sp_process_payment` — capture payment, advance order PLACED -> PAID
- `sp_create_shipment` — create shipment, advance PAID -> SHIPPED
- `sp_mark_shipment_delivered` — mark delivered, advance SHIPPED -> DELIVERED
- `sp_update_order_status` — validated status transition + history
- `sp_cancel_order` — cancel + restore inventory (cursor over order items)
- `sp_create_return` — create return, advance to RETURNED
- `sp_submit_review` — upsert review + recalculate product rating
- `sp_refund_payment` — refund a captured payment
- `sp_adjust_inventory` / `sp_reserve_stock` — stock mutations with guards

## SIGNAL-based Business-Rule Validation

| Prefix              | Example condition                                       |
|---------------------|---------------------------------------------------------|
| INSUFFICIENT_STOCK  | add-to-cart / order line exceeds available stock        |
| DISCOUNT_INVALID    | code missing, inactive, expired, over limit, below min  |
| INVALID_STATUS      | illegal order status transition; inactive product/cust  |
| NOT_FOUND           | product / order / inventory / payment / shipment missing|
| PAYMENT_ERROR       | payment less than total; refunding a non-captured payment|
| INVALID_AMOUNT      | non-positive quantity/amount; empty cart; bad rating    |

## Stored Procedure Catalog (85 routines)

### Category (1-7)
sp_create_category, sp_get_category_by_id, sp_get_all_categories,
sp_get_subcategories, sp_update_category_status, fn_get_category_product_count,
sp_get_category_tree

### Customer (8-16)
sp_create_customer, sp_get_customer_by_id, sp_get_customer_by_email,
sp_update_customer_contact, sp_update_customer_tier, sp_search_customers,
fn_get_customer_order_count, fn_get_customer_lifetime_value,
sp_update_customer_status

### Product (17-28)
sp_create_product, sp_get_product_by_id, sp_get_product_by_sku,
sp_search_products_by_category, sp_search_products, sp_update_product_price,
sp_update_product_status, sp_get_products_price_range, sp_get_top_rated_products,
fn_get_product_price, fn_discounted_price, sp_recalculate_product_rating

### Inventory (29-38)
sp_set_inventory, sp_get_inventory_by_product, sp_get_inventory_by_warehouse,
sp_adjust_inventory, sp_get_low_stock_report, fn_get_total_stock, fn_is_in_stock,
sp_reserve_stock, sp_release_stock, sp_get_warehouse_stock_value

### Cart (39-46)
sp_get_or_create_cart, sp_add_to_cart, sp_update_cart_item_quantity,
sp_remove_cart_item, sp_get_cart_items, fn_get_cart_total, fn_get_cart_item_count,
sp_clear_cart

### Discount (47-53)
sp_create_discount_code, sp_get_discount_by_code, sp_validate_discount,
sp_get_active_discounts, sp_disable_discount, sp_expire_discounts,
fn_is_discount_valid

### Order (54-70)
sp_place_order, sp_get_order_by_id, sp_get_order_by_ref, sp_get_order_items,
sp_get_orders_by_customer, sp_get_orders_by_status, sp_update_order_status,
sp_cancel_order, sp_get_order_status_history, fn_get_order_item_count,
fn_get_order_status, sp_get_recent_orders, sp_get_orders_date_range,
sp_get_order_full, sp_count_orders_by_status, sp_get_top_customers_by_spend,
sp_get_customer_order_summary

### Payment (71-76)
sp_process_payment, sp_get_payment_by_id, sp_get_payments_by_order,
sp_refund_payment, fn_get_order_paid_amount, sp_get_payments_by_status

### Shipment (77-81)
sp_create_shipment, sp_get_shipment_by_id, sp_get_shipments_by_order,
sp_mark_shipment_delivered, sp_update_shipment_status

### Return / Review / Report (82-85)
sp_create_return, sp_submit_review, sp_get_reviews_by_product, sp_get_sales_report

## Seed Data Volume

| Entity          | Rows |
|-----------------|------|
| categories      | 10   |
| warehouses      | 4    |
| customers       | 35   |
| products        | 120  |
| inventory       | 160  |
| discount_codes  | 7    |
| orders          | 160  |
| order_items     | 420  |
| payments        | 128  |
| shipments       | 96   |
| returns         | 16   |
| reviews         | 60   |

Products span all 10 categories with realistic prices; products 30-39 are
intentionally seeded with low stock (0-5 units, below the reorder level of 10)
for low-stock report testing. Orders are distributed across all statuses
(PLACED, PAID, SHIPPED, DELIVERED, CANCELLED, RETURNED) with matching payments,
shipments and returns. Discount code `EXPIRED5` is seeded expired for
negative-path testing.
