CREATE TABLE servicemind.support_tickets (
    id                          BIGSERIAL PRIMARY KEY,
    ticket_no                   VARCHAR(32) NOT NULL,
    customer_id                 BIGINT NOT NULL,
    order_id                    BIGINT,
    after_sales_application_id  BIGINT,
    category                    VARCHAR(32) NOT NULL,
    priority                    VARCHAR(16) NOT NULL,
    status                      VARCHAR(32) NOT NULL,
    source                      VARCHAR(32) NOT NULL,
    subject                     VARCHAR(200) NOT NULL,
    description                 VARCHAR(2000) NOT NULL,
    assigned_to                 VARCHAR(100),
    resolution                  VARCHAR(2000),
    opened_at                   TIMESTAMPTZ NOT NULL,
    first_responded_at          TIMESTAMPTZ,
    assigned_at                 TIMESTAMPTZ,
    resolved_at                 TIMESTAMPTZ,
    closed_at                   TIMESTAMPTZ,
    version                     BIGINT NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ NOT NULL
                                DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ NOT NULL
                                DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_support_ticket_no
        UNIQUE (ticket_no),

    CONSTRAINT fk_support_ticket_customer
        FOREIGN KEY (customer_id)
        REFERENCES servicemind.customers (id),

    CONSTRAINT fk_support_ticket_order
        FOREIGN KEY (order_id)
        REFERENCES servicemind.customer_orders (id),

    CONSTRAINT fk_support_ticket_after_sales
        FOREIGN KEY (after_sales_application_id)
        REFERENCES servicemind.after_sales_applications (id),

    CONSTRAINT ck_support_ticket_category
        CHECK (
            category IN (
                'ORDER',
                'PAYMENT',
                'DELIVERY',
                'RETURN_EXCHANGE',
                'REPAIR',
                'PRODUCT',
                'ACCOUNT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_support_ticket_priority
        CHECK (
            priority IN (
                'LOW',
                'NORMAL',
                'HIGH',
                'URGENT'
            )
        ),

    CONSTRAINT ck_support_ticket_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'WAITING_CUSTOMER',
                'RESOLVED',
                'CLOSED'
            )
        ),

    CONSTRAINT ck_support_ticket_source
        CHECK (
            source IN (
                'WEB',
                'CHAT',
                'PHONE',
                'EMAIL',
                'AI',
                'SYSTEM'
            )
        )
);

CREATE INDEX idx_support_tickets_customer_id
    ON servicemind.support_tickets (customer_id);

CREATE INDEX idx_support_tickets_order_id
    ON servicemind.support_tickets (order_id);

CREATE INDEX idx_support_tickets_after_sales_id
    ON servicemind.support_tickets (
        after_sales_application_id
    );

CREATE INDEX idx_support_tickets_status
    ON servicemind.support_tickets (status);

CREATE INDEX idx_support_tickets_priority
    ON servicemind.support_tickets (priority);

CREATE INDEX idx_support_tickets_assigned_to
    ON servicemind.support_tickets (assigned_to);

CREATE INDEX idx_support_tickets_opened_at
    ON servicemind.support_tickets (opened_at);

CREATE INDEX idx_support_tickets_customer_status
    ON servicemind.support_tickets (
        customer_id,
        status
    );