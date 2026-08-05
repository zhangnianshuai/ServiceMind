package com.zhangnianshuai.servicemind.api.order;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.order.dto.OrderDetailResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderSummaryResponse;
import com.zhangnianshuai.servicemind.application.order.OrderQueryService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/{orderNo}")
    public OrderDetailResponse getOrder(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderQueryService.getOrderByOrderNo(orderNo);
    }

    @GetMapping
    public List<OrderSummaryResponse> getOrdersByCustomer(
            @RequestParam
            @NotBlank
            @Size(max = 32)
            String customerNo) {
        return orderQueryService.getOrdersByCustomerNo(customerNo);
    }
}