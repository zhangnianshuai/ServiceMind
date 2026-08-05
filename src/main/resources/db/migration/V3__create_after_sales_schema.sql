CREATE TABLE servicemind.after_sales_applications (
    id                      BIGSERIAL PRIMARY KEY,
    application_no          VARCHAR(32) NOT NULL,
    order_id                BIGINT NOT NULL,
    order_item_id           BIGINT NOT NULL,
    type                    VARCHAR(32) NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    requested_quantity      INTEGER NOT NULL,
    reason                  VARCHAR(200) NOT NULL,
    description             VARCHAR(1000),
    review_comment          VARCHAR(500),
    submitted_at            TIMESTAMPTZ NOT NULL,
    reviewed_at             TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_after_sales_application_no
        UNIQUE (application_no),

    CONSTRAINT fk_after_sales_order
        FOREIGN KEY (order_id)
        REFERENCES servicemind.customer_orders (id),

    CONSTRAINT fk_after_sales_order_item
        FOREIGN KEY (order_item_id)
        REFERENCES servicemind.order_items (id),

    CONSTRAINT ck_after_sales_type
        CHECK (
            type IN (
                'RETURN',
                'EXCHANGE',
                'REPAIR'
            )
        ),

    CONSTRAINT ck_after_sales_status
        CHECK (
            status IN (
                'SUBMITTED',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'PROCESSING',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_after_sales_requested_quantity
        CHECK (requested_quantity > 0)
);

CREATE INDEX idx_after_sales_order_id
    ON servicemind.after_sales_applications (order_id);

CREATE INDEX idx_after_sales_order_item_id
    ON servicemind.after_sales_applications (order_item_id);

CREATE INDEX idx_after_sales_status
    ON servicemind.after_sales_applications (status);

CREATE INDEX idx_after_sales_submitted_at
    ON servicemind.after_sales_applications (submitted_at);

CREATE UNIQUE INDEX uk_after_sales_active_order_item
    ON servicemind.after_sales_applications (order_item_id)
    WHERE status IN (
        'SUBMITTED',
        'UNDER_REVIEW',
        'APPROVED',
        'PROCESSING'
    );