package com.zhangnianshuai.servicemind.api.order;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.order.dto.CreateOrderRequest;
import com.zhangnianshuai.servicemind.api.order.dto.OrderDetailResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderPageResponse;
import com.zhangnianshuai.servicemind.api.order.dto.OrderSummaryResponse;
import com.zhangnianshuai.servicemind.application.order.OrderCommandService;
import com.zhangnianshuai.servicemind.application.order.OrderQueryService;
import com.zhangnianshuai.servicemind.domain.order.OrderStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    public OrderQueryController(
            OrderQueryService orderQueryService,
            OrderCommandService orderCommandService) {
        this.orderQueryService = orderQueryService;
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponse> createOrder(
            @Valid
            @RequestBody
            CreateOrderRequest request) {
        OrderDetailResponse response =
            orderCommandService.create(request);

        URI location = URI.create(
            "/api/orders/" + response.orderNo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/{orderNo}")
    public OrderDetailResponse getOrder(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderQueryService.getOrderByOrderNo(
            orderNo
        );
    }

    @GetMapping
    public List<OrderSummaryResponse>
            getOrdersByCustomer(
                @RequestParam
                @NotBlank
                @Size(max = 32)
                String customerNo) {
        return orderQueryService
            .getOrdersByCustomerNo(customerNo);
    }

    @GetMapping("/search")
    public OrderPageResponse searchOrders(
            @RequestParam(required = false)
            @Size(max = 32)
            String orderNo,

            @RequestParam(required = false)
            @Size(max = 32)
            String customerNo,

            @RequestParam(required = false)
            OrderStatus status,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size) {

        return orderQueryService.searchOrders(
            orderNo,
            customerNo,
            status,
            page,
            size
        );
    }
    @PostMapping("/{orderNo}/pay")
    public OrderDetailResponse pay(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderCommandService.pay(orderNo);
    }

    @PostMapping("/{orderNo}/ship")
    public OrderDetailResponse ship(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderCommandService.ship(orderNo);
    }

    @PostMapping("/{orderNo}/complete")
    public OrderDetailResponse complete(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderCommandService.complete(orderNo);
    }

    @PostMapping("/{orderNo}/cancel")
    public OrderDetailResponse cancel(
            @PathVariable
            @NotBlank
            @Size(max = 32)
            String orderNo) {
        return orderCommandService.cancel(orderNo);
    }
}