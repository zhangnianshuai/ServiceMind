package com.zhangnianshuai.servicemind.api.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.OrderStatus;

public record OrderSummaryResponse(
    String orderNo,
    String customerNo,
    String customerName,
    OrderStatus status,
    BigDecimal totalAmount,
    OffsetDateTime orderedAt
) {

    public static OrderSummaryResponse from(CustomerOrder order) {
        return new OrderSummaryResponse(
            order.getOrderNo(),
            order.getCustomer().getCustomerNo(),
            order.getCustomer().getFullName(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getOrderedAt()
        );
    }
}