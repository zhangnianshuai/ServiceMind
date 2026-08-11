package com.zhangnianshuai.servicemind.application.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhangnianshuai.servicemind.api.order.dto.OrderDetailResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderPageResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderSummaryResponse;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrderRepository;
import com.zhangnianshuai.servicemind.domain.order.OrderStatus;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final CustomerOrderRepository orderRepository;

    public OrderQueryService(
            CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailResponse getOrderByOrderNo(
            String orderNo) {

        String normalizedOrderNo =
            normalizeRequired(
                orderNo,
                "Order number"
            );

        return orderRepository
            .findByOrderNoIgnoreCase(
                normalizedOrderNo
            )
            .map(OrderDetailResponse::from)
            .orElseThrow(
                () -> new OrderNotFoundException(
                    normalizedOrderNo
                )
            );
    }

    public List<OrderSummaryResponse>
            getOrdersByCustomerNo(
                String customerNo) {

        String normalizedCustomerNo =
            normalizeRequired(
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

    public OrderPageResponse searchOrders(
            String orderNo,
            String customerNo,
            OrderStatus status,
            int page,
            int size) {

        String normalizedOrderNo =
            normalizeOptional(orderNo);

        String normalizedCustomerNo =
            normalizeOptional(customerNo);

        PageRequest pageable =
            PageRequest.of(
                page,
                size,
                Sort.by(
                    Sort.Direction.DESC,
                    "orderedAt"
                )
            );

        Page<OrderSummaryResponse> result =
            orderRepository
                .searchOrders(
                    normalizedOrderNo,
                    normalizedCustomerNo,
                    status,
                    pageable
                )
                .map(OrderSummaryResponse::from);

        return OrderPageResponse.from(result);
    }

    private String normalizeRequired(
            String value,
            String fieldName) {

        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(
                fieldName
                    + " must not be blank."
            );
        }

        return value.trim();
    }


    private String toLikePattern(String value) {
        if (!StringUtils.hasText(value)) {
            return "%";
        }

        return "%"
            + value.trim()
                .toLowerCase(java.util.Locale.ROOT)
            + "%";
    }
    private String normalizeOptional(
            String value) {

        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}