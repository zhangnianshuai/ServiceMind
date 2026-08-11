package com.zhangnianshuai.servicemind.api.aftersales.dto;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesStatus;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesType;

public record AfterSalesResponse(

    String applicationNo,
    String orderNo,
    String customerNo,
    String customerName,
    Long orderItemId,
    String sku,
    String productName,
    AfterSalesType type,
    AfterSalesStatus status,
    Integer requestedQuantity,
    String reason,
    String description,
    String reviewComment,
    OffsetDateTime submittedAt,
    OffsetDateTime reviewedAt,
    OffsetDateTime completedAt,
    OffsetDateTime warrantyExpiresAt
) {
}