CREATE SCHEMA IF NOT EXISTS servicemind;

CREATE TABLE servicemind.customers (
    id              BIGSERIAL PRIMARY KEY,
    customer_no     VARCHAR(32) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(32),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_customers_customer_no UNIQUE (customer_no),
    CONSTRAINT uk_customers_email UNIQUE (email)
);

CREATE TABLE servicemind.products (
    id                  BIGSERIAL PRIMARY KEY,
    sku                 VARCHAR(64) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    category            VARCHAR(100) NOT NULL,
    description         TEXT,
    price               NUMERIC(12, 2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    warranty_months     INTEGER NOT NULL DEFAULT 12,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price
        CHECK (price >= 0),
    CONSTRAINT ck_products_warranty_months
        CHECK (warranty_months >= 0),
    CONSTRAINT ck_products_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE servicemind.customer_orders (
    id                  BIGSERIAL PRIMARY KEY,
    order_no            VARCHAR(32) NOT NULL,
    customer_id         BIGINT NOT NULL,
    status              VARCHAR(32) NOT NULL,
    total_amount        NUMERIC(12, 2) NOT NULL,
    ordered_at          TIMESTAMPTZ NOT NULL,
    paid_at             TIMESTAMPTZ,
    shipped_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_customer_orders_order_no UNIQUE (order_no),
    CONSTRAINT fk_customer_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES servicemind.customers (id),
    CONSTRAINT ck_customer_orders_total_amount
        CHECK (total_amount >= 0),
    CONSTRAINT ck_customer_orders_status
        CHECK (
            status IN (
                'CREATED',
                'PAID',
                'SHIPPED',
                'COMPLETED',
                'CANCELLED'
            )
        )
);

CREATE TABLE servicemind.order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL,
    product_id              BIGINT NOT NULL,
    sku_snapshot            VARCHAR(64) NOT NULL,
    product_name_snapshot   VARCHAR(200) NOT NULL,
    unit_price              NUMERIC(12, 2) NOT NULL,
    quantity                INTEGER NOT NULL,
    line_amount             NUMERIC(12, 2) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES servicemind.customer_orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES servicemind.products (id),
    CONSTRAINT ck_order_items_unit_price
        CHECK (unit_price >= 0),
    CONSTRAINT ck_order_items_quantity
        CHECK (quantity > 0),
    CONSTRAINT ck_order_items_line_amount
        CHECK (line_amount >= 0)
);

CREATE INDEX idx_customer_orders_customer_id
    ON servicemind.customer_orders (customer_id);

CREATE INDEX idx_customer_orders_status
    ON servicemind.customer_orders (status);

CREATE INDEX idx_customer_orders_ordered_at
    ON servicemind.customer_orders (ordered_at);

CREATE INDEX idx_order_items_order_id
    ON servicemind.order_items (order_id);

CREATE INDEX idx_order_items_product_id
    ON servicemind.order_items (product_id);

CREATE INDEX idx_products_category
    ON servicemind.products (category);

CREATE INDEX idx_products_status
    ON servicemind.products (status);