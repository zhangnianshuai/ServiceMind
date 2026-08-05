package com.zhangnianshuai.servicemind.api.order.dto;

import java.math.BigDecimal;

import com.zhangnianshuai.servicemind.domain.order.OrderItem;

public record OrderItemResponse(
    String sku,
    String productName,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal lineAmount
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
            item.getSkuSnapshot(),
            item.getProductNameSnapshot(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineAmount()
        );
    }
}