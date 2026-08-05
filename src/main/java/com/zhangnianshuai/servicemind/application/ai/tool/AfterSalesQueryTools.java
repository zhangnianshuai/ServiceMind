package com.zhangnianshuai.servicemind.application.ai.tool;

import java.util.function.Supplier;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.application.aftersales.AfterSalesApplicationService;

@Component
public class AfterSalesQueryTools {

    private final AfterSalesApplicationService service;

    public AfterSalesQueryTools(
            AfterSalesApplicationService service) {
        this.service = service;
    }

    @Tool(
        name = "get_after_sales_detail",
        description = """
            Get the complete details and current status of
            one after-sales application.
            Use this tool when an application number is known.
            """
    )
    public BusinessToolResult getAfterSalesDetail(
            @ToolParam(
                description =
                    "After-sales application number beginning with AS"
            )
            String applicationNo) {
        return execute(
            "get_after_sales_detail",
            () -> service.findByApplicationNo(applicationNo)
        );
    }

    @Tool(
        name = "list_customer_after_sales",
        description = """
            List all after-sales applications belonging to
            one customer, including returns, exchanges and repairs.
            """
    )
    public BusinessToolResult listCustomerAfterSales(
            @ToolParam(
                description =
                    "Customer number, for example CUST-001"
            )
            String customerNo) {
        return execute(
            "list_customer_after_sales",
            () -> service.findByCustomerNo(customerNo)
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