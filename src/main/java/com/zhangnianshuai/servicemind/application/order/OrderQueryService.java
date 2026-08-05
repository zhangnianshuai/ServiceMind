package com.zhangnianshuai.servicemind.application.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhangnianshuai.servicemind.api.order.dto.OrderDetailResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderSummaryResponse;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrderRepository;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final CustomerOrderRepository orderRepository;

    public OrderQueryService(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailResponse getOrderByOrderNo(String orderNo) {
        String normalizedOrderNo = normalize(orderNo, "Order number");

        return orderRepository
            .findByOrderNoIgnoreCase(normalizedOrderNo)
            .map(OrderDetailResponse::from)
            .orElseThrow(
                () -> new OrderNotFoundException(normalizedOrderNo)
            );
    }

    public List<OrderSummaryResponse> getOrdersByCustomerNo(
            String customerNo) {
        String normalizedCustomerNo = normalize(
            customerNo,
            "Customer number"
        );

        return orderRepository
            .findByCustomer_CustomerNoIgnoreCaseOrderByOrderedAtDesc(
                normalizedCustomerNo
            )
            .stream()
            .map(OrderSummaryResponse::from)
            .toList();
    }

    private String normalize(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " must not be blank."
            );
        }

        return value.trim();
    }
}