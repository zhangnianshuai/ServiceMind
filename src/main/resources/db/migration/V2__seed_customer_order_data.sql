-- Demo customers
INSERT INTO servicemind.customers (
    customer_no,
    full_name,
    email,
    phone
)
VALUES
    (
        'CUST-001',
        '张伟',
        'zhangwei@example.com',
        '13800000001'
    ),
    (
        'CUST-002',
        '李娜',
        'lina@example.com',
        '13800000002'
    ),
    (
        'CUST-003',
        '王强',
        'wangqiang@example.com',
        '13800000003'
    );

-- Demo products
INSERT INTO servicemind.products (
    sku,
    name,
    category,
    description,
    price,
    status,
    warranty_months
)
VALUES
    (
        'MON-X200',
        'X200 27英寸显示器',
        '显示器',
        '支持2K分辨率与高刷新率的办公显示器',
        1999.00,
        'ACTIVE',
        36
    ),
    (
        'KB-K87',
        'K87机械键盘',
        '键盘',
        '87键热插拔机械键盘',
        399.00,
        'ACTIVE',
        12
    ),
    (
        'LAP-L15',
        'L15轻薄笔记本',
        '笔记本电脑',
        '面向办公场景的15英寸轻薄笔记本',
        6999.00,
        'ACTIVE',
        24
    ),
    (
        'MOU-M5',
        'M5无线鼠标',
        '鼠标',
        '支持蓝牙和无线双模连接',
        259.00,
        'ACTIVE',
        12
    ),
    (
        'HUB-H8',
        'H8多功能扩展坞',
        '电脑配件',
        '支持HDMI、USB和网络接口扩展',
        499.00,
        'ACTIVE',
        18
    );

-- Demo orders
INSERT INTO servicemind.customer_orders (
    order_no,
    customer_id,
    status,
    total_amount,
    ordered_at,
    paid_at,
    shipped_at,
    completed_at
)
SELECT
    'ORD-20260001',
    c.id,
    'COMPLETED',
    2258.00,
    TIMESTAMPTZ '2026-01-10 10:00:00+08',
    TIMESTAMPTZ '2026-01-10 10:05:00+08',
    TIMESTAMPTZ '2026-01-11 09:00:00+08',
    TIMESTAMPTZ '2026-01-14 15:30:00+08'
FROM servicemind.customers c
WHERE c.customer_no = 'CUST-001';

INSERT INTO servicemind.customer_orders (
    order_no,
    customer_id,
    status,
    total_amount,
    ordered_at,
    paid_at
)
SELECT
    'ORD-20260002',
    c.id,
    'PAID',
    399.00,
    TIMESTAMPTZ '2026-07-28 14:00:00+08',
    TIMESTAMPTZ '2026-07-28 14:03:00+08'
FROM servicemind.customers c
WHERE c.customer_no = 'CUST-001';

INSERT INTO servicemind.customer_orders (
    order_no,
    customer_id,
    status,
    total_amount,
    ordered_at,
    paid_at,
    shipped_at
)
SELECT
    'ORD-20260003',
    c.id,
    'SHIPPED',
    7498.00,
    TIMESTAMPTZ '2026-07-30 09:20:00+08',
    TIMESTAMPTZ '2026-07-30 09:22:00+08',
    TIMESTAMPTZ '2026-08-01 11:00:00+08'
FROM servicemind.customers c
WHERE c.customer_no = 'CUST-002';

INSERT INTO servicemind.customer_orders (
    order_no,
    customer_id,
    status,
    total_amount,
    ordered_at
)
SELECT
    'ORD-20260004',
    c.id,
    'CREATED',
    259.00,
    TIMESTAMPTZ '2026-08-04 18:30:00+08'
FROM servicemind.customers c
WHERE c.customer_no = 'CUST-003';

-- Order ORD-20260001: display + mouse
INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    1999.00,
    1,
    1999.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'MON-X200'
WHERE o.order_no = 'ORD-20260001';

INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    259.00,
    1,
    259.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'MOU-M5'
WHERE o.order_no = 'ORD-20260001';

-- Order ORD-20260002: keyboard
INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    399.00,
    1,
    399.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'KB-K87'
WHERE o.order_no = 'ORD-20260002';

-- Order ORD-20260003: laptop + hub
INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    6999.00,
    1,
    6999.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'LAP-L15'
WHERE o.order_no = 'ORD-20260003';

INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    499.00,
    1,
    499.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'HUB-H8'
WHERE o.order_no = 'ORD-20260003';

-- Order ORD-20260004: mouse
INSERT INTO servicemind.order_items (
    order_id,
    product_id,
    sku_snapshot,
    product_name_snapshot,
    unit_price,
    quantity,
    line_amount
)
SELECT
    o.id,
    p.id,
    p.sku,
    p.name,
    259.00,
    1,
    259.00
FROM servicemind.customer_orders o
JOIN servicemind.products p
    ON p.sku = 'MOU-M5'
WHERE o.order_no = 'ORD-20260004';