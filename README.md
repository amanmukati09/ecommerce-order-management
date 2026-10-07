# Ecommerce Order Management

An e-commerce order management backend built as a **legacy modernization training
fixture**. It uses a classic (pre-Spring-Boot) Java EE / Spring MVC stack with a
mixed persistence strategy: Hibernate for entity CRUD and Spring JDBC for all
stored-procedure interactions, including the transactional order-placement logic.

- **~10,000 lines** of Java + SQL
- **85 MySQL stored procedures / functions** (72 procedures + 13 scalar functions)
- **15 tables** with proper foreign keys, indexes, and `DECIMAL(10,2)` money columns
- **Real transactional logic** in the database tier (atomic order placement with
  inventory check + decrement + discount application) with `SIGNAL SQLSTATE`
  business-rule validation
- **Fully deployed and curl-tested** end to end against real seed data

---

## Tech Stack (strict)

| Layer            | Technology                                             |
|------------------|--------------------------------------------------------|
| Language         | Java 8                                                 |
| Web framework    | Spring MVC 5.3.x (XML + annotation config, NOT Boot)   |
| Entity ORM       | Hibernate 5.6.x (Customer, Product, Category, Review)  |
| Stored procedures| Spring JDBC `JdbcTemplate` + `SimpleJdbcCall`          |
| Packaging        | WAR, deployed to Tomcat 9                              |
| Database         | MySQL 8.x                                              |
| Build            | Maven (single module)                                  |
| Logging          | Logback                                                |
| Tests            | JUnit 4 + Mockito                                      |

### Persistence separation (important)

The two DAO families are **kept in separate packages** and never mix concerns:

- `com.ecommerce.dao.hibernate` — Hibernate `SessionFactory` based CRUD for
  `Customer`, `Product`, `Category`, `Review`.
- `com.ecommerce.dao.jdbc` — Spring JDBC only. Every stored procedure call
  (order placement, inventory, payments, shipments, discounts, reporting, scalar
  functions) lives here. No Hibernate access occurs in this package.

### Transactional approach (the chosen convention)

**Order placement is handled entirely inside one stored procedure**
(`sp_place_order`), not by chaining multiple procedure calls from a Java
`@Transactional` method. The procedure performs, atomically:

1. Validate the customer is active and the cart is non-empty.
2. Compute the subtotal from cart items.
3. Validate and apply a discount code (if provided), capping it to limits.
4. Compute tax (8% of the discounted subtotal) and the grand total.
5. Create the `orders` row and all `order_items`.
6. For each line, check available stock and **decrement inventory** greedily
   across warehouses (preferring the chosen warehouse).
7. Increment the discount's usage counter.
8. Convert the cart (`OPEN` -> `CONVERTED`) and write an order status-history row.

The whole body runs in `START TRANSACTION ... COMMIT` with
`DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END`, so any
failure (including a `SIGNAL`) rolls the entire order back. The other
transactional procedures (payment capture, shipment creation, delivery,
status transitions, cancellation with inventory restoration, returns, review
submission, inventory adjust/reserve) follow the same pattern.

Business-rule violations are raised with `SIGNAL SQLSTATE '45000'` and a prefixed
`MESSAGE_TEXT`. The DAO layer catches the exception and `SqlErrorTranslator`
converts it into a typed exception, so a raw `SQLException` never reaches the
controller:

| SIGNAL prefix       | Java exception                      | HTTP status |
|---------------------|-------------------------------------|-------------|
| INSUFFICIENT_STOCK  | InsufficientStockException          | 422         |
| DISCOUNT_INVALID    | DiscountInvalidException            | 422         |
| INVALID_STATUS      | InvalidStatusTransitionException    | 409         |
| NOT_FOUND           | ResourceNotFoundException           | 404         |
| PAYMENT_ERROR       | PaymentException                    | 422         |
| INVALID_AMOUNT      | InvalidAmountException              | 400         |

---

## Project Layout

```
ecommerce-order-mgmt/
├── pom.xml
├── src/main/java/com/ecommerce/
│   ├── entity/           # Hibernate entities (Customer, Product, Category, Review)
│   ├── model/            # Read-model POJOs from procedure result sets
│   ├── dao/hibernate/    # Hibernate DAOs (entity CRUD only)
│   ├── dao/jdbc/         # Spring JDBC DAOs (stored procedure calls only)
│   ├── service/          # Transactional service layer
│   ├── controller/       # @RestController REST endpoints
│   ├── exception/        # Typed exceptions + SqlErrorTranslator
│   ├── dto/              # Response envelope
│   ├── util/             # Static helpers (price formatter, warehouse router)
│   └── web/              # Request logging interceptor
├── src/main/resources/
│   ├── logback.xml
│   └── sql/
│       ├── schema.sql     # 15 tables + FKs + indexes + DECIMAL(10,2) money
│       ├── procedures.sql # 85 stored procedures / functions
│       └── data.sql       # Seed data (120 products, 35 customers, 160 orders...)
├── src/main/webapp/WEB-INF/
│   ├── web.xml
│   ├── applicationContext.xml   # DataSource, SessionFactory, JdbcTemplate, tx
│   └── dispatcher-servlet.xml   # Spring MVC + Jackson JSON + CORS + interceptor
└── src/test/java/...            # JUnit 4 tests
```

---

## 1. MySQL Setup

Load the SQL scripts **in order** (`schema` → `procedures` → `data`).

```bash
mysql -u root -p < src/main/resources/sql/schema.sql
mysql -u root -p < src/main/resources/sql/procedures.sql
mysql -u root -p < src/main/resources/sql/data.sql
```

> `procedures.sql` uses `DELIMITER $$ ... DELIMITER ;` around every
> `CREATE PROCEDURE`/`CREATE FUNCTION`, required because the bodies contain
> semicolons. Load it with the `mysql` CLI.

Create the application user (the app connects as `eom_app`):

```sql
CREATE USER 'eom_app'@'localhost' IDENTIFIED WITH mysql_native_password BY 'EomApp_2026!';
GRANT ALL PRIVILEGES ON ecommerce_order.* TO 'eom_app'@'localhost';
GRANT CREATE ROUTINE, ALTER ROUTINE, EXECUTE ON ecommerce_order.* TO 'eom_app'@'localhost';
FLUSH PRIVILEGES;
```

If MySQL blocks creating functions with binary logging enabled:

```sql
SET GLOBAL log_bin_trust_function_creators = 1;
```

Verify the load:

```sql
USE ecommerce_order;
SELECT COUNT(*) FROM information_schema.tables   WHERE table_schema='ecommerce_order';   -- 15
SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema='ecommerce_order'; -- 85
SELECT COUNT(*) FROM products;    -- 120
SELECT COUNT(*) FROM orders;      -- 160
SELECT COUNT(*) FROM inventory WHERE quantity <= reorder_level; -- 10 (low-stock)
```

The datasource settings live in `src/main/webapp/WEB-INF/applicationContext.xml`
(`jdbcUrl`, `username`, `password`). Adjust them if your MySQL host/credentials
differ.

---

## 2. Build

```bash
mvn clean package
```

Compiles, runs the JUnit 4 tests, and produces `target/ecommerce-order-mgmt.war`.
To skip tests: `mvn clean package -DskipTests`.

---

## 3. Deploy to Tomcat 9

```bash
echo 'export JAVA_HOME=/path/to/java-8' > $CATALINA_HOME/bin/setenv.sh
cp target/ecommerce-order-mgmt.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

The app deploys under the context path `/ecommerce-order-mgmt`; all REST
endpoints are served under `/ecommerce-order-mgmt/api/...`.

> On the training EC2 host, Tomcat's HTTP connector is on **port 8081**, so the
> base URL there is `http://localhost:8081/ecommerce-order-mgmt/api`.

Health check:

```bash
curl http://localhost:8081/ecommerce-order-mgmt/api/health
```

---

## 4. REST API

Base URL: `http://<host>:8081/ecommerce-order-mgmt/api`

### Catalog & Products
| Method | Path                                        | Description               |
|--------|---------------------------------------------|---------------------------|
| GET    | `/catalog/categories`                       | All categories            |
| GET    | `/catalog/categories/{id}`                  | Category by id            |
| GET    | `/catalog/categories/{id}/subcategories`    | Subcategories             |
| GET    | `/catalog/customers/{id}`                   | Customer by id            |
| GET    | `/products/{id}`                            | Product detail            |
| GET    | `/products/category/{categoryId}`           | Products by category      |
| GET    | `/products/search?q=`                       | Search products           |
| GET    | `/products/price-range?min=&max=`           | Products in a price range |
| GET    | `/products/top-rated?limit=`                | Top-rated products        |
| GET    | `/products/{id}/stock`                      | Total available stock     |
| GET    | `/products/{id}/discounted-price?code=`     | Price after a discount    |
| PUT    | `/products/{id}/price`                      | Update price              |

### Cart
| Method | Path                              | Description          |
|--------|-----------------------------------|----------------------|
| POST   | `/cart/add`                       | Add to cart (stock-checked) |
| GET    | `/cart/customer/{customerId}`     | View cart            |
| PUT    | `/cart/item/{cartItemId}`         | Update quantity      |
| DELETE | `/cart/item/{cartItemId}`         | Remove item          |
| DELETE | `/cart/{cartId}`                  | Clear cart           |

### Orders
| Method | Path                              | Description                          |
|--------|-----------------------------------|--------------------------------------|
| POST   | `/orders`                         | Place order (atomic, inventory + discount) |
| GET    | `/orders/{id}`                    | Order + line items                   |
| GET    | `/orders/{id}/status`             | Status + history                     |
| GET    | `/orders/customer/{customerId}`   | Orders for a customer                |
| GET    | `/orders/status/{status}`         | Orders by status                     |
| GET    | `/orders/recent`                  | Recent orders                        |
| PUT    | `/orders/{id}/status`             | Update status (validated transition) |
| POST   | `/orders/{id}/cancel`             | Cancel (restores inventory)          |
| GET    | `/orders/stats/by-status`         | Order counts/value by status         |

### Payments
| Method | Path                        | Description                     |
|--------|-----------------------------|---------------------------------|
| POST   | `/payments`                 | Process payment (advances to PAID) |
| POST   | `/payments/{id}/refund`     | Refund a payment                |
| GET    | `/payments/{id}`            | Payment detail                  |
| GET    | `/payments/order/{orderId}` | Payments for an order           |

### Shipments
| Method | Path                          | Description                     |
|--------|-------------------------------|---------------------------------|
| POST   | `/shipments`                  | Create shipment (advances to SHIPPED) |
| PUT    | `/shipments/{id}/deliver`     | Mark delivered (advances to DELIVERED)|
| GET    | `/shipments/{id}`             | Shipment detail                 |
| GET    | `/shipments/order/{orderId}`  | Shipments for an order          |

### Discounts
| Method | Path                   | Description                       |
|--------|------------------------|-----------------------------------|
| POST   | `/discounts/apply`     | Validate + compute discount       |
| GET    | `/discounts/{code}`    | Discount detail                   |
| GET    | `/discounts/active`    | Active discounts                  |

### Reviews
| Method | Path                          | Description                        |
|--------|-------------------------------|------------------------------------|
| POST   | `/reviews`                    | Submit review (recalcs product rating) |
| GET    | `/reviews/product/{productId}`| Reviews for a product              |

### Inventory & Reports
| Method | Path                                     | Description             |
|--------|------------------------------------------|-------------------------|
| GET    | `/inventory/low-stock?limit=`            | Low-stock report        |
| GET    | `/inventory/product/{productId}`         | Inventory per warehouse |
| GET    | `/inventory/warehouse/{id}/value`        | Warehouse stock value   |
| POST   | `/inventory/adjust`                      | Adjust stock            |
| POST   | `/reports/returns`                       | Create a return/refund  |
| GET    | `/reports/sales?from=&to=`               | Sales by category       |
| GET    | `/reports/top-customers?limit=`          | Top customers by spend  |

---

## 5. Example curl calls

```bash
BASE=http://localhost:8081/ecommerce-order-mgmt/api

# Browse a category
curl "$BASE/products/category/1?limit=5"

# Add to cart (stock is validated inside the procedure)
curl -X POST "$BASE/cart/add" -H "Content-Type: application/json" \
  -d '{"customerId":15,"productId":60,"quantity":2}'

# Place an order (atomic: inventory decrement + discount + tax + line items)
curl -X POST "$BASE/orders" -H "Content-Type: application/json" \
  -d '{"customerId":15,"cartId":5,"warehouseId":1,"discountCode":"WELCOME10","shippingAddress":"9 Poplar Way","shippingAmount":9.99}'

# Process payment -> advances order to PAID
curl -X POST "$BASE/payments" -H "Content-Type: application/json" \
  -d '{"orderId":161,"amount":114.20,"method":"CARD"}'

# Create shipment -> SHIPPED, then mark delivered -> DELIVERED
curl -X POST "$BASE/shipments" -H "Content-Type: application/json" \
  -d '{"orderId":161,"warehouseId":1,"carrier":"FedEx","trackingNumber":"TRK123","shippingCost":9.99}'
curl -X PUT "$BASE/shipments/97/deliver"

# Low-stock inventory report
curl "$BASE/inventory/low-stock?limit=10"

# Expired discount -> HTTP 422 DISCOUNT_INVALID
curl -X POST "$BASE/discounts/apply" -H "Content-Type: application/json" \
  -d '{"code":"EXPIRED5","orderAmount":100.00}'
```

---

## Stored Procedure Calling Convention

- **Result-set-returning procedures** are called via
  `jdbcTemplate.query("CALL proc(?)", args, rowMapper)`.
- **OUT-parameter / write procedures** use `SimpleJdbcCall` with explicitly
  declared parameters.
- **Scalar functions** (`fn_*`) are called with `SELECT fn_name(?)` — e.g.
  `fn_discounted_price(product_id, code)` computes a discounted price.
- When a procedure raises `SIGNAL SQLSTATE '45000'`, the DAO catches it and
  `SqlErrorTranslator` converts it to a typed `EcommerceException` subtype.

---

## Deliberate Legacy Smells

As a modernization training fixture, the codebase includes intentional
(but non-breaking) legacy patterns:

- **Static utility class for price/currency formatting** — `PriceFormatter`
  uses a shared, non-thread-safe `DecimalFormat` guarded by `synchronized` and a
  hand-rolled currency-symbol `switch`.
- **Warehouse-region branching logic** — `WarehouseRegionRouter` encodes
  preferred-warehouse, delivery-estimate and free-shipping rules as conditional
  ladders on the region string.
- **Broad catch-all exception handler** — `GlobalExceptionHandler` includes a
  final `@ExceptionHandler(Exception.class)` mapping everything else to HTTP 500.

These run correctly but are the kind of thing a modernization effort would refactor.
