package com.zhangnianshuai.servicemind.application.ai.tool;

import java.util.function.Supplier;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.application.order.OrderQueryService;

@Component
public class OrderQueryTools {

    private final OrderQueryService orderQueryService;

    public OrderQueryTools(
            OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Tool(
        name = "get_order_detail",
        description = """
            Get the complete details of one customer order,
            including customer, status, amount, timestamps,
            products, quantities and prices.
            Use this tool only when an order number is known.
            """
    )
    public BusinessToolResult getOrderDetail(
            @ToolParam(
                description =
                    "Order number, for example ORD-20260001"
            )
            String orderNo) {
        return execute(
            "get_order_detail",
            () -> orderQueryService.getOrderByOrderNo(orderNo)
        );
    }

    @Tool(
        name = "list_customer_orders",
        description = """
            List all orders belonging to one customer,
            ordered from newest to oldest.
            Use this tool when a customer number is known.
            """
    )
    public BusinessToolResult listCustomerOrders(
            @ToolParam(
                description =
                    "Customer number, for example CUST-001"
            )
            String customerNo) {
        return execute(
            "list_customer_orders",
            () -> orderQueryService
                .getOrdersByCustomerNo(customerNo)
        );
    }

    private BusinessToolResult execute(
            String operation,
            Supplier<?> supplier) {
        try {
            return BusinessToolResult.success(
                operation,
                supplier.get()
            );
        }
        catch (ResponseStatusException exception) {
            return BusinessToolResult.failure(
                operation,
                exception.getReason()
            );
        }
        catch (RuntimeException exception) {
            return BusinessToolResult.failure(
                operation,
                exception.getMessage()
            );
        }
    }
}