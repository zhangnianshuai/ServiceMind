package com.zhangnianshuai.servicemind.api.order.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.OrderStatus;

public record OrderDetailResponse(
    String orderNo,
    String customerNo,
    String customerName,
    OrderStatus status,
    BigDecimal totalAmount,
    OffsetDateTime orderedAt,
    OffsetDateTime paidAt,
    OffsetDateTime shippedAt,
    OffsetDateTime completedAt,
    List<OrderItemResponse> items
) {

    public static OrderDetailResponse from(CustomerOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems()
            .stream()
            .map(OrderItemResponse::from)
            .toList();

        return new OrderDetailResponse(
            order.getOrderNo(),
            order.getCustomer().getCustomerNo(),
            order.getCustomer().getFullName(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getOrderedAt(),
            order.getPaidAt(),
            order.getShippedAt(),
            order.getCompletedAt(),
            itemResponses
        );
    }
}