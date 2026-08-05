package com.zhangnianshuai.servicemind.application.ai.tool;

import java.util.function.Supplier;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.application.ticket.SupportTicketService;

@Component
public class SupportTicketQueryTools {

    private final SupportTicketService service;

    public SupportTicketQueryTools(
            SupportTicketService service) {
        this.service = service;
    }

    @Tool(
        name = "get_support_ticket_detail",
        description = """
            Get the complete details and current status of
            one customer support ticket.
            Use this tool when a ticket number is known.
            """
    )
    public BusinessToolResult getSupportTicketDetail(
            @ToolParam(
                description =
                    "Support ticket number beginning with TK"
            )
            String ticketNo) {
        return execute(
            "get_support_ticket_detail",
            () -> service.findByTicketNo(ticketNo)
        );
    }

    @Tool(
        name = "list_customer_support_tickets",
        description = """
            List all customer support tickets belonging to
            one customer, ordered from newest to oldest.
            """
    )
    public BusinessToolResult listCustomerSupportTickets(
            @ToolParam(
                description =
                    "Customer number, for example CUST-001"
            )
            String customerNo) {
        return execute(
            "list_customer_support_tickets",
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