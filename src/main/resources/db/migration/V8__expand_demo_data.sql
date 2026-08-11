-- V8__expand_demo_data.sql
-- ServiceMind demo dataset expansion.
-- Knowledge documents are seeded as DRAFT intentionally; publish them through the app later
-- so the real chunking/embedding/pgvector pipeline creates vectors.

INSERT INTO servicemind.customers (customer_no, full_name, email, phone) VALUES
    ('CUST-901', '陈晨', 'chenchen@example.com', '13800000004'),
    ('CUST-902', '赵敏', 'zhaomin@example.com', '13800000005'),
    ('CUST-903', '刘洋', 'liuyang@example.com', '13800000006'),
    ('CUST-904', '孙悦', 'sunyue@example.com', '13800000007'),
    ('CUST-905', '周凯', 'zhoukai@example.com', '13800000008'),
    ('CUST-906', '吴婷', 'wuting@example.com', '13800000009'),
    ('CUST-907', '郑浩', 'zhenghao@example.com', '13800000010'),
    ('CUST-908', '冯雪', 'fengxue@example.com', '13800000011'),
    ('CUST-909', '何俊', 'hejun@example.com', '13800000012'),
    ('CUST-910', '郭琳', 'guolin@example.com', '13800000013'),
    ('CUST-911', '马超', 'machao@example.com', '13800000014'),
    ('CUST-912', '林静', 'linjing@example.com', '13800000015');

INSERT INTO servicemind.products (sku, name, category, description, price, status, warranty_months) VALUES
    ('HEAD-H9', 'H9降噪无线耳机', '音频设备', '支持主动降噪、蓝牙多设备切换与有线模式', 699.00, 'ACTIVE', 12),
    ('CAM-C4', 'C4高清摄像头', '办公外设', '支持1080P视频会议与自动曝光调节', 459.00, 'ACTIVE', 12),
    ('SSD-S1T', 'S1T移动固态硬盘', '存储设备', '1TB高速便携固态硬盘，支持USB-C接口', 599.00, 'ACTIVE', 36),
    ('RAM-R32', 'R32 32GB内存套装', '电脑配件', '双通道32GB内存套装，适用于主流办公主机', 799.00, 'ACTIVE', 36),
    ('ROU-R6', 'R6双频无线路由器', '网络设备', '支持Wi-Fi 6与多设备并发接入', 699.00, 'ACTIVE', 24),
    ('SPK-S2', 'S2桌面音箱', '音频设备', '双单元桌面音箱，支持蓝牙与有线输入', 329.00, 'ACTIVE', 12),
    ('MIC-M8', 'M8 USB会议麦克风', '办公外设', '适用于远程会议与直播场景的USB麦克风', 899.00, 'ACTIVE', 12),
    ('TAB-T11', 'T11平板电脑', '平板电脑', '11英寸高分辨率平板，支持键盘与触控笔扩展', 3299.00, 'ACTIVE', 24),
    ('CHA-C1', 'C1人体工学办公椅', '办公家具', '多档腰托与扶手调节的人体工学办公椅', 1599.00, 'ACTIVE', 24),
    ('BAG-B15', 'B15电脑通勤包', '电脑配件', '适用于15英寸笔记本的防泼水通勤背包', 199.00, 'ACTIVE', 6),
    ('PWR-P100', 'P100 100W氮化镓充电器', '电脑配件', '三口100W氮化镓快速充电器', 349.00, 'ACTIVE', 12);

WITH order_seed (order_no, customer_no, status, ordered_at, paid_at, shipped_at, completed_at) AS (
    VALUES
        ('ORD-20260901', 'CUST-901', 'COMPLETED', TIMESTAMPTZ '2026-02-03 09:20:00+08', TIMESTAMPTZ '2026-02-03 09:23:00+08', TIMESTAMPTZ '2026-02-04 14:10:00+08', TIMESTAMPTZ '2026-02-07 18:30:00+08'),
        ('ORD-20260902', 'CUST-902', 'COMPLETED', TIMESTAMPTZ '2026-02-18 19:40:00+08', TIMESTAMPTZ '2026-02-18 19:42:00+08', TIMESTAMPTZ '2026-02-19 16:00:00+08', TIMESTAMPTZ '2026-02-22 11:15:00+08'),
        ('ORD-20260903', 'CUST-903', 'SHIPPED', TIMESTAMPTZ '2026-07-31 10:10:00+08', TIMESTAMPTZ '2026-07-31 10:12:00+08', TIMESTAMPTZ '2026-08-02 08:40:00+08', NULL::TIMESTAMPTZ),
        ('ORD-20260904', 'CUST-904', 'PAID', TIMESTAMPTZ '2026-08-08 20:05:00+08', TIMESTAMPTZ '2026-08-08 20:07:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260905', 'CUST-905', 'CREATED', TIMESTAMPTZ '2026-08-11 08:30:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260906', 'CUST-906', 'CANCELLED', TIMESTAMPTZ '2026-07-20 13:45:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260907', 'CUST-907', 'COMPLETED', TIMESTAMPTZ '2026-03-06 12:00:00+08', TIMESTAMPTZ '2026-03-06 12:03:00+08', TIMESTAMPTZ '2026-03-07 09:30:00+08', TIMESTAMPTZ '2026-03-10 17:20:00+08'),
        ('ORD-20260908', 'CUST-908', 'COMPLETED', TIMESTAMPTZ '2026-03-19 15:25:00+08', TIMESTAMPTZ '2026-03-19 15:26:00+08', TIMESTAMPTZ '2026-03-20 10:40:00+08', TIMESTAMPTZ '2026-03-23 14:50:00+08'),
        ('ORD-20260909', 'CUST-909', 'SHIPPED', TIMESTAMPTZ '2026-08-05 09:10:00+08', TIMESTAMPTZ '2026-08-05 09:12:00+08', TIMESTAMPTZ '2026-08-06 17:35:00+08', NULL::TIMESTAMPTZ),
        ('ORD-20260910', 'CUST-910', 'PAID', TIMESTAMPTZ '2026-08-10 11:40:00+08', TIMESTAMPTZ '2026-08-10 11:42:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260911', 'CUST-911', 'COMPLETED', TIMESTAMPTZ '2026-04-09 08:15:00+08', TIMESTAMPTZ '2026-04-09 08:18:00+08', TIMESTAMPTZ '2026-04-10 13:05:00+08', TIMESTAMPTZ '2026-04-13 16:30:00+08'),
        ('ORD-20260912', 'CUST-912', 'CREATED', TIMESTAMPTZ '2026-08-11 09:50:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260913', 'CUST-901', 'COMPLETED', TIMESTAMPTZ '2026-05-02 10:30:00+08', TIMESTAMPTZ '2026-05-02 10:33:00+08', TIMESTAMPTZ '2026-05-03 15:10:00+08', TIMESTAMPTZ '2026-05-06 12:10:00+08'),
        ('ORD-20260914', 'CUST-902', 'SHIPPED', TIMESTAMPTZ '2026-08-04 17:25:00+08', TIMESTAMPTZ '2026-08-04 17:29:00+08', TIMESTAMPTZ '2026-08-05 14:20:00+08', NULL::TIMESTAMPTZ),
        ('ORD-20260915', 'CUST-903', 'COMPLETED', TIMESTAMPTZ '2026-05-21 14:00:00+08', TIMESTAMPTZ '2026-05-21 14:04:00+08', TIMESTAMPTZ '2026-05-22 11:40:00+08', TIMESTAMPTZ '2026-05-25 19:05:00+08'),
        ('ORD-20260916', 'CUST-904', 'CANCELLED', TIMESTAMPTZ '2026-06-02 21:10:00+08', TIMESTAMPTZ '2026-06-02 21:12:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260917', 'CUST-905', 'COMPLETED', TIMESTAMPTZ '2026-06-11 10:05:00+08', TIMESTAMPTZ '2026-06-11 10:07:00+08', TIMESTAMPTZ '2026-06-12 09:20:00+08', TIMESTAMPTZ '2026-06-15 13:45:00+08'),
        ('ORD-20260918', 'CUST-906', 'PAID', TIMESTAMPTZ '2026-08-09 16:30:00+08', TIMESTAMPTZ '2026-08-09 16:32:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260919', 'CUST-907', 'SHIPPED', TIMESTAMPTZ '2026-08-06 13:00:00+08', TIMESTAMPTZ '2026-08-06 13:01:00+08', TIMESTAMPTZ '2026-08-07 18:10:00+08', NULL::TIMESTAMPTZ),
        ('ORD-20260920', 'CUST-908', 'COMPLETED', TIMESTAMPTZ '2026-06-22 18:25:00+08', TIMESTAMPTZ '2026-06-22 18:27:00+08', TIMESTAMPTZ '2026-06-23 15:20:00+08', TIMESTAMPTZ '2026-06-26 10:30:00+08'),
        ('ORD-20260921', 'CUST-909', 'CREATED', TIMESTAMPTZ '2026-08-11 10:20:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260922', 'CUST-910', 'COMPLETED', TIMESTAMPTZ '2026-07-02 09:00:00+08', TIMESTAMPTZ '2026-07-02 09:03:00+08', TIMESTAMPTZ '2026-07-03 12:35:00+08', TIMESTAMPTZ '2026-07-06 16:40:00+08'),
        ('ORD-20260923', 'CUST-911', 'PAID', TIMESTAMPTZ '2026-08-10 20:55:00+08', TIMESTAMPTZ '2026-08-10 20:57:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('ORD-20260924', 'CUST-912', 'COMPLETED', TIMESTAMPTZ '2026-07-11 11:15:00+08', TIMESTAMPTZ '2026-07-11 11:18:00+08', TIMESTAMPTZ '2026-07-12 10:25:00+08', TIMESTAMPTZ '2026-07-15 18:00:00+08'),
        ('ORD-20260925', 'CUST-901', 'SHIPPED', TIMESTAMPTZ '2026-08-07 09:45:00+08', TIMESTAMPTZ '2026-08-07 09:47:00+08', TIMESTAMPTZ '2026-08-08 14:20:00+08', NULL::TIMESTAMPTZ),
        ('ORD-20260926', 'CUST-902', 'COMPLETED', TIMESTAMPTZ '2026-07-25 16:10:00+08', TIMESTAMPTZ '2026-07-25 16:12:00+08', TIMESTAMPTZ '2026-07-26 13:40:00+08', TIMESTAMPTZ '2026-07-29 09:35:00+08')
)
INSERT INTO servicemind.customer_orders (order_no, customer_id, status, total_amount, ordered_at, paid_at, shipped_at, completed_at)
SELECT s.order_no, c.id, s.status, 0.00, s.ordered_at, s.paid_at, s.shipped_at, s.completed_at
FROM order_seed s JOIN servicemind.customers c ON c.customer_no = s.customer_no;

WITH item_seed (order_no, sku, quantity) AS (
    VALUES
        ('ORD-20260901', 'HEAD-H9', 1),
        ('ORD-20260901', 'CAM-C4', 1),
        ('ORD-20260902', 'LAP-L15', 1),
        ('ORD-20260902', 'BAG-B15', 1),
        ('ORD-20260903', 'TAB-T11', 1),
        ('ORD-20260903', 'PWR-P100', 1),
        ('ORD-20260904', 'SSD-S1T', 2),
        ('ORD-20260905', 'ROU-R6', 1),
        ('ORD-20260906', 'MIC-M8', 1),
        ('ORD-20260907', 'MON-X200', 1),
        ('ORD-20260907', 'KB-K87', 1),
        ('ORD-20260907', 'MOU-M5', 1),
        ('ORD-20260908', 'CHA-C1', 1),
        ('ORD-20260909', 'RAM-R32', 1),
        ('ORD-20260909', 'SSD-S1T', 1),
        ('ORD-20260910', 'SPK-S2', 1),
        ('ORD-20260910', 'MOU-M5', 1),
        ('ORD-20260911', 'CAM-C4', 1),
        ('ORD-20260911', 'MIC-M8', 1),
        ('ORD-20260912', 'HUB-H8', 1),
        ('ORD-20260912', 'PWR-P100', 1),
        ('ORD-20260913', 'TAB-T11', 1),
        ('ORD-20260913', 'HEAD-H9', 1),
        ('ORD-20260914', 'MON-X200', 1),
        ('ORD-20260914', 'HUB-H8', 1),
        ('ORD-20260915', 'KB-K87', 1),
        ('ORD-20260915', 'MOU-M5', 1),
        ('ORD-20260916', 'LAP-L15', 1),
        ('ORD-20260917', 'SSD-S1T', 1),
        ('ORD-20260917', 'RAM-R32', 1),
        ('ORD-20260917', 'HUB-H8', 1),
        ('ORD-20260918', 'ROU-R6', 1),
        ('ORD-20260918', 'SPK-S2', 1),
        ('ORD-20260919', 'CAM-C4', 1),
        ('ORD-20260919', 'HEAD-H9', 1),
        ('ORD-20260920', 'MIC-M8', 1),
        ('ORD-20260920', 'SPK-S2', 1),
        ('ORD-20260921', 'PWR-P100', 1),
        ('ORD-20260921', 'BAG-B15', 1),
        ('ORD-20260922', 'CHA-C1', 1),
        ('ORD-20260922', 'MON-X200', 1),
        ('ORD-20260923', 'TAB-T11', 1),
        ('ORD-20260924', 'LAP-L15', 1),
        ('ORD-20260924', 'SSD-S1T', 1),
        ('ORD-20260925', 'KB-K87', 1),
        ('ORD-20260925', 'MIC-M8', 1),
        ('ORD-20260926', 'HUB-H8', 1),
        ('ORD-20260926', 'ROU-R6', 1),
        ('ORD-20260926', 'PWR-P100', 1)
)
INSERT INTO servicemind.order_items (order_id, product_id, sku_snapshot, product_name_snapshot, unit_price, quantity, line_amount)
SELECT o.id, p.id, p.sku, p.name, p.price, s.quantity, p.price * s.quantity
FROM item_seed s
JOIN servicemind.customer_orders o ON o.order_no = s.order_no
JOIN servicemind.products p ON p.sku = s.sku;

UPDATE servicemind.customer_orders o
SET total_amount = totals.total_amount, updated_at = CURRENT_TIMESTAMP
FROM (
    SELECT oi.order_id, SUM(oi.line_amount) AS total_amount
    FROM servicemind.order_items oi
    JOIN servicemind.customer_orders co ON co.id = oi.order_id
    WHERE co.order_no BETWEEN 'ORD-20260901' AND 'ORD-20260926'
    GROUP BY oi.order_id
) totals
WHERE o.id = totals.order_id;

WITH after_sales_seed (application_no, order_no, sku, type, status, requested_quantity, reason, description, review_comment, submitted_at, reviewed_at, completed_at) AS (
    VALUES
        ('AS-20260901', 'ORD-20260901', 'CAM-C4', 'EXCHANGE', 'COMPLETED', 1, '摄像头画面间歇性闪烁', '客户反馈视频会议过程中画面偶发闪烁，希望更换同型号商品。', '检测确认设备异常，同意换货。', TIMESTAMPTZ '2026-02-09 10:20:00+08', TIMESTAMPTZ '2026-02-09 14:00:00+08', TIMESTAMPTZ '2026-02-14 16:30:00+08'),
        ('AS-20260902', 'ORD-20260902', 'BAG-B15', 'RETURN', 'UNDER_REVIEW', 1, '尺寸不符合使用需求', '客户申请退回未使用的电脑通勤包。', NULL, TIMESTAMPTZ '2026-08-09 09:30:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('AS-20260903', 'ORD-20260907', 'MON-X200', 'REPAIR', 'PROCESSING', 1, '显示器出现亮线', '使用数月后屏幕右侧出现固定亮线。', '符合保修条件，已进入维修流程。', TIMESTAMPTZ '2026-08-02 11:10:00+08', TIMESTAMPTZ '2026-08-02 15:40:00+08', NULL::TIMESTAMPTZ),
        ('AS-20260904', 'ORD-20260908', 'CHA-C1', 'RETURN', 'REJECTED', 1, '使用后感觉坐姿不适', '客户收货较长时间后申请无理由退货。', '已超过无理由退货时限，商品不存在质量问题。', TIMESTAMPTZ '2026-04-15 10:00:00+08', TIMESTAMPTZ '2026-04-15 16:10:00+08', NULL::TIMESTAMPTZ),
        ('AS-20260905', 'ORD-20260911', 'MIC-M8', 'EXCHANGE', 'APPROVED', 1, '麦克风底噪较明显', '客户在会议环境下测试发现持续底噪。', '初步判定硬件异常，同意换货。', TIMESTAMPTZ '2026-08-08 13:30:00+08', TIMESTAMPTZ '2026-08-08 17:00:00+08', NULL::TIMESTAMPTZ),
        ('AS-20260906', 'ORD-20260913', 'TAB-T11', 'REPAIR', 'SUBMITTED', 1, '平板无法正常充电', '更换充电器后问题仍然存在，申请检测维修。', NULL, TIMESTAMPTZ '2026-08-11 09:00:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('AS-20260907', 'ORD-20260915', 'MOU-M5', 'RETURN', 'COMPLETED', 1, '鼠标滚轮失灵', '客户申请质量问题退货。', '确认质量问题，同意退货退款。', TIMESTAMPTZ '2026-06-01 10:25:00+08', TIMESTAMPTZ '2026-06-01 15:10:00+08', TIMESTAMPTZ '2026-06-05 11:45:00+08'),
        ('AS-20260908', 'ORD-20260917', 'SSD-S1T', 'REPAIR', 'PROCESSING', 1, '移动硬盘偶发无法识别', '客户反馈在多台电脑上均出现间歇性无法识别。', '已收件检测，等待维修结果。', TIMESTAMPTZ '2026-08-03 09:35:00+08', TIMESTAMPTZ '2026-08-03 14:20:00+08', NULL::TIMESTAMPTZ),
        ('AS-20260909', 'ORD-20260920', 'MIC-M8', 'EXCHANGE', 'CANCELLED', 1, '配件疑似缺失', '客户提交换货后自行确认配件在包装夹层中。', NULL, TIMESTAMPTZ '2026-07-01 18:05:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('AS-20260910', 'ORD-20260922', 'CHA-C1', 'RETURN', 'UNDER_REVIEW', 1, '椅背调节机构异常', '椅背锁止后仍会发生位移，客户申请退货。', NULL, TIMESTAMPTZ '2026-08-10 10:15:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('AS-20260911', 'ORD-20260924', 'LAP-L15', 'REPAIR', 'APPROVED', 1, '笔记本风扇异响', '高负载运行时风扇出现持续异响。', '符合保修条件，同意寄修。', TIMESTAMPTZ '2026-08-07 14:30:00+08', TIMESTAMPTZ '2026-08-07 18:00:00+08', NULL::TIMESTAMPTZ),
        ('AS-20260912', 'ORD-20260926', 'HUB-H8', 'EXCHANGE', 'COMPLETED', 1, 'HDMI接口无视频输出', '扩展坞其他接口正常，但HDMI无信号。', '检测确认接口故障，已完成换货。', TIMESTAMPTZ '2026-07-31 09:50:00+08', TIMESTAMPTZ '2026-07-31 14:40:00+08', TIMESTAMPTZ '2026-08-04 16:25:00+08')
)
INSERT INTO servicemind.after_sales_applications (application_no, order_id, order_item_id, type, status, requested_quantity, reason, description, review_comment, submitted_at, reviewed_at, completed_at)
SELECT s.application_no, o.id, oi.id, s.type, s.status, s.requested_quantity, s.reason, s.description, s.review_comment, s.submitted_at, s.reviewed_at, s.completed_at
FROM after_sales_seed s
JOIN servicemind.customer_orders o ON o.order_no = s.order_no
JOIN servicemind.order_items oi ON oi.order_id = o.id AND oi.sku_snapshot = s.sku;

WITH ticket_seed (ticket_no, customer_no, order_no, application_no, category, priority, status, source, subject, description, assigned_to, resolution, opened_at, first_responded_at, assigned_at, resolved_at, closed_at) AS (
    VALUES
        ('TKT-20260901', 'CUST-901', 'ORD-20260901', 'AS-20260901', 'RETURN_EXCHANGE', 'NORMAL', 'CLOSED', 'CHAT', '摄像头换货进度咨询', '客户咨询摄像头换货后的重新发货时间。', '客服-林晓', '已完成换货并向客户同步新物流单号。', TIMESTAMPTZ '2026-02-10 09:10:00+08', TIMESTAMPTZ '2026-02-10 09:14:00+08', TIMESTAMPTZ '2026-02-10 09:15:00+08', TIMESTAMPTZ '2026-02-14 16:40:00+08', TIMESTAMPTZ '2026-02-15 09:20:00+08'),
        ('TKT-20260902', 'CUST-902', 'ORD-20260902', 'AS-20260902', 'RETURN_EXCHANGE', 'NORMAL', 'IN_PROGRESS', 'WEB', '退货申请审核咨询', '客户希望确认通勤包退货申请预计审核时间。', '客服-周宁', NULL, TIMESTAMPTZ '2026-08-09 10:05:00+08', TIMESTAMPTZ '2026-08-09 10:20:00+08', TIMESTAMPTZ '2026-08-09 10:22:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260903', 'CUST-903', 'ORD-20260903', NULL, 'DELIVERY', 'HIGH', 'OPEN', 'CHAT', '平板订单物流长时间未更新', '物流信息两天未更新，客户担心包裹异常。', NULL, NULL, TIMESTAMPTZ '2026-08-11 08:45:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260904', 'CUST-904', 'ORD-20260904', NULL, 'ORDER', 'NORMAL', 'WAITING_CUSTOMER', 'EMAIL', '修改订单收货信息', '订单已付款但尚未发货，客户申请修改联系电话。', '客服-许雯', NULL, TIMESTAMPTZ '2026-08-09 08:50:00+08', TIMESTAMPTZ '2026-08-09 09:10:00+08', TIMESTAMPTZ '2026-08-09 09:12:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260905', 'CUST-905', 'ORD-20260905', NULL, 'PAYMENT', 'LOW', 'RESOLVED', 'AI', '订单付款方式咨询', '客户询问创建订单后支持哪些付款方式。', 'AI客服', '已说明当前支持的支付渠道及付款时限。', TIMESTAMPTZ '2026-08-11 08:35:00+08', TIMESTAMPTZ '2026-08-11 08:35:00+08', TIMESTAMPTZ '2026-08-11 08:35:00+08', TIMESTAMPTZ '2026-08-11 08:37:00+08', NULL::TIMESTAMPTZ),
        ('TKT-20260906', 'CUST-906', 'ORD-20260906', NULL, 'ORDER', 'LOW', 'CLOSED', 'PHONE', '确认订单取消结果', '客户来电确认未付款订单是否已经成功取消。', '客服-林晓', '已确认订单处于取消状态，不会继续履约。', TIMESTAMPTZ '2026-07-20 14:10:00+08', TIMESTAMPTZ '2026-07-20 14:11:00+08', TIMESTAMPTZ '2026-07-20 14:11:00+08', TIMESTAMPTZ '2026-07-20 14:15:00+08', TIMESTAMPTZ '2026-07-20 14:20:00+08'),
        ('TKT-20260907', 'CUST-907', 'ORD-20260907', 'AS-20260903', 'REPAIR', 'HIGH', 'IN_PROGRESS', 'WEB', '显示器维修状态查询', '客户希望了解显示器检测和预计维修完成时间。', '客服-周宁', NULL, TIMESTAMPTZ '2026-08-05 10:40:00+08', TIMESTAMPTZ '2026-08-05 10:55:00+08', TIMESTAMPTZ '2026-08-05 11:00:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260908', 'CUST-908', 'ORD-20260908', 'AS-20260904', 'RETURN_EXCHANGE', 'NORMAL', 'RESOLVED', 'CHAT', '退货申请被拒绝原因', '客户咨询办公椅退货申请被拒绝的具体依据。', '客服-许雯', '已说明退货时限及质量问题判定规则，客户表示理解。', TIMESTAMPTZ '2026-04-16 09:35:00+08', TIMESTAMPTZ '2026-04-16 09:40:00+08', TIMESTAMPTZ '2026-04-16 09:42:00+08', TIMESTAMPTZ '2026-04-16 10:10:00+08', NULL::TIMESTAMPTZ),
        ('TKT-20260909', 'CUST-909', 'ORD-20260909', NULL, 'DELIVERY', 'URGENT', 'IN_PROGRESS', 'PHONE', '快递显示异常签收', '客户表示本人未签收，但物流状态显示已签收。', '客服-林晓', NULL, TIMESTAMPTZ '2026-08-11 09:15:00+08', TIMESTAMPTZ '2026-08-11 09:16:00+08', TIMESTAMPTZ '2026-08-11 09:17:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260910', 'CUST-910', 'ORD-20260910', NULL, 'ORDER', 'NORMAL', 'OPEN', 'WEB', '订单何时安排发货', '客户已经付款，希望确认预计发货时间。', NULL, NULL, TIMESTAMPTZ '2026-08-11 10:05:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260911', 'CUST-911', 'ORD-20260911', 'AS-20260905', 'RETURN_EXCHANGE', 'HIGH', 'WAITING_CUSTOMER', 'EMAIL', '麦克风换货寄回方式', '售后审核已通过，等待客户确认取件地址。', '客服-周宁', NULL, TIMESTAMPTZ '2026-08-09 13:20:00+08', TIMESTAMPTZ '2026-08-09 13:35:00+08', TIMESTAMPTZ '2026-08-09 13:37:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260912', 'CUST-912', 'ORD-20260912', NULL, 'PRODUCT', 'LOW', 'RESOLVED', 'AI', '扩展坞是否兼容笔记本', '客户下单前咨询H8扩展坞的接口兼容情况。', 'AI客服', '已根据产品资料说明接口要求与兼容条件。', TIMESTAMPTZ '2026-08-11 09:55:00+08', TIMESTAMPTZ '2026-08-11 09:55:00+08', TIMESTAMPTZ '2026-08-11 09:55:00+08', TIMESTAMPTZ '2026-08-11 09:57:00+08', NULL::TIMESTAMPTZ),
        ('TKT-20260913', 'CUST-901', 'ORD-20260913', 'AS-20260906', 'REPAIR', 'HIGH', 'OPEN', 'CHAT', '平板维修申请后如何寄送', '客户刚提交维修申请，询问后续寄送方式。', NULL, NULL, TIMESTAMPTZ '2026-08-11 09:20:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260914', 'CUST-905', 'ORD-20260917', 'AS-20260908', 'REPAIR', 'NORMAL', 'IN_PROGRESS', 'WEB', '移动硬盘维修检测进度', '客户希望了解硬盘检测结果以及数据安全情况。', '客服-许雯', NULL, TIMESTAMPTZ '2026-08-07 15:40:00+08', TIMESTAMPTZ '2026-08-07 15:52:00+08', TIMESTAMPTZ '2026-08-07 15:55:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260915', 'CUST-910', 'ORD-20260922', 'AS-20260910', 'RETURN_EXCHANGE', 'HIGH', 'OPEN', 'WEB', '办公椅退货申请补充材料', '客户询问是否需要上传椅背调节机构异常的视频。', NULL, NULL, TIMESTAMPTZ '2026-08-11 10:35:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260916', 'CUST-912', 'ORD-20260924', 'AS-20260911', 'REPAIR', 'URGENT', 'IN_PROGRESS', 'PHONE', '笔记本寄修影响工作急需处理', '客户工作设备出现风扇异响，希望加快寄修处理。', '客服-林晓', NULL, TIMESTAMPTZ '2026-08-08 09:05:00+08', TIMESTAMPTZ '2026-08-08 09:06:00+08', TIMESTAMPTZ '2026-08-08 09:07:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260917', 'CUST-901', 'ORD-20260925', NULL, 'DELIVERY', 'NORMAL', 'WAITING_CUSTOMER', 'CHAT', '订单配送地址二次确认', '物流方反馈地址缺少楼栋信息，需要客户补充。', '客服-周宁', NULL, TIMESTAMPTZ '2026-08-09 11:15:00+08', TIMESTAMPTZ '2026-08-09 11:18:00+08', TIMESTAMPTZ '2026-08-09 11:20:00+08', NULL::TIMESTAMPTZ, NULL::TIMESTAMPTZ),
        ('TKT-20260918', 'CUST-902', 'ORD-20260926', 'AS-20260912', 'RETURN_EXCHANGE', 'NORMAL', 'CLOSED', 'EMAIL', '扩展坞换货完成确认', '客户确认新扩展坞HDMI接口工作正常。', '客服-许雯', '客户已确认更换后的设备正常，工单关闭。', TIMESTAMPTZ '2026-08-05 10:00:00+08', TIMESTAMPTZ '2026-08-05 10:15:00+08', TIMESTAMPTZ '2026-08-05 10:17:00+08', TIMESTAMPTZ '2026-08-05 10:30:00+08', TIMESTAMPTZ '2026-08-05 10:35:00+08')
)
INSERT INTO servicemind.support_tickets (ticket_no, customer_id, order_id, after_sales_application_id, category, priority, status, source, subject, description, assigned_to, resolution, opened_at, first_responded_at, assigned_at, resolved_at, closed_at)
SELECT s.ticket_no, c.id, o.id, a.id, s.category, s.priority, s.status, s.source, s.subject, s.description, s.assigned_to, s.resolution, s.opened_at, s.first_responded_at, s.assigned_at, s.resolved_at, s.closed_at
FROM ticket_seed s
JOIN servicemind.customers c ON c.customer_no = s.customer_no
LEFT JOIN servicemind.customer_orders o ON o.order_no = s.order_no
LEFT JOIN servicemind.after_sales_applications a ON a.application_no = s.application_no;

WITH knowledge_seed (document_no, title, category, raw_content) AS (
    VALUES
        ('KB-DEMO-001', '退货与换货服务政策', 'RETURN_EXCHANGE', '退货与换货服务适用于符合条件的已完成订单。客户提交申请时应选择具体订单商品并说明原因。质量问题可申请退货或换货，非质量问题需同时满足退货时限和商品完好要求。审核通过后，客服会告知寄回方式。换货商品在原商品验收后安排重新发出。'),
        ('KB-DEMO-002', '产品保修与维修政策', 'WARRANTY', '不同产品的保修期以商品页面及订单商品信息为准。保修期内发生非人为硬件故障时，可提交维修申请。维修申请审核通过后，客户按照客服指引寄送设备。检测完成后会更新处理结果；超出保修范围或属于人为损坏时，客服会在维修前说明可能产生的费用。'),
        ('KB-DEMO-003', '订单支付、取消与状态说明', 'ORDER_PAYMENT', '订单创建后处于CREATED状态，支付成功后进入PAID状态。仓库发货后状态变为SHIPPED，客户完成收货后进入COMPLETED。CREATED订单可直接取消；已支付但尚未发货的订单可申请取消。已发货订单通常需要在收货后按照售后流程处理。'),
        ('KB-DEMO-004', '物流配送与异常签收处理', 'GENERAL', '订单发货后可通过订单详情查看物流状态。物流长时间不更新、显示异常签收或配送地址信息不完整时，可以创建客服工单。客服会根据订单号核对物流节点并联系承运方。异常签收场景下，建议客户同时确认家人、前台或代收点是否已经代收。'),
        ('KB-DEMO-005', 'X200显示器常见问题处理', 'PRODUCT', 'X200显示器支持2K分辨率与高刷新率。出现无信号时，应先确认视频线、输入源和电脑输出接口；出现亮线、坏点或持续闪烁时，可尝试更换视频线与设备进行交叉测试。若问题持续且仍在保修期内，可提交维修申请并提供故障照片或视频。'),
        ('KB-DEMO-006', 'L15轻薄笔记本使用与售后说明', 'PRODUCT', 'L15轻薄笔记本适用于办公和日常学习。首次使用建议完成系统更新并保持散热口畅通。若出现充电异常、风扇持续异响、无法启动等硬件问题，可先完成基础排查。问题仍存在时，应通过订单中的具体商品提交维修申请。'),
        ('KB-DEMO-007', '账户与客服安全提示', 'ACCOUNT', '客服不会通过聊天、电话或邮件索取用户的支付密码、完整银行卡密码或模型API Key。用户使用BYOK模式时，API Key仅用于临时模型会话，不应在普通聊天文本中发送。发现异常登录或可疑信息时，应立即停止操作并联系官方客服。'),
        ('KB-DEMO-008', '维修服务处理流程', 'REPAIR', '维修流程通常包括提交申请、审核、寄送设备、检测、维修和完成六个阶段。客户应在申请中准确描述故障现象。审核通过后按客服提供的地址寄送。维修处理中可通过售后申请或关联工单查询进度。维修完成后会同步结果和返还物流信息。')
), inserted_documents AS (
    INSERT INTO servicemind.knowledge_documents (document_no, title, category, status, latest_version_no, active_version_no, created_by)
    SELECT s.document_no, s.title, s.category, 'DRAFT', 1, NULL, 'system-demo' FROM knowledge_seed s
    RETURNING id, document_no
)
INSERT INTO servicemind.knowledge_document_versions (document_id, version_no, source_type, original_filename, mime_type, content_hash, raw_content, status, chunk_count)
SELECT d.id, 1, 'TEXT', NULL, 'text/plain', md5(s.raw_content) || md5(s.document_no || s.raw_content), s.raw_content, 'DRAFT', 0
FROM knowledge_seed s JOIN inserted_documents d ON d.document_no = s.document_no;

-- Optional manual inspection after migration:
SELECT 'customers' AS entity, COUNT(*) AS row_count FROM servicemind.customers
UNION ALL SELECT 'products', COUNT(*) FROM servicemind.products
UNION ALL SELECT 'orders', COUNT(*) FROM servicemind.customer_orders
UNION ALL SELECT 'order_items', COUNT(*) FROM servicemind.order_items
UNION ALL SELECT 'after_sales', COUNT(*) FROM servicemind.after_sales_applications
UNION ALL SELECT 'tickets', COUNT(*) FROM servicemind.support_tickets
UNION ALL SELECT 'knowledge_documents', COUNT(*) FROM servicemind.knowledge_documents;
