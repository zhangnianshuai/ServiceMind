package com.zhangnianshuai.servicemind.api.ticket.dto;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.ticket.TicketCategory;
import com.zhangnianshuai.servicemind.domain.ticket.TicketPriority;
import com.zhangnianshuai.servicemind.domain.ticket.TicketSource;
import com.zhangnianshuai.servicemind.domain.ticket.TicketStatus;

public record SupportTicketResponse(

    String ticketNo,
    String customerNo,
    String customerName,
    String orderNo,
    String afterSalesApplicationNo,
    TicketCategory category,
    TicketPriority priority,
    TicketStatus status,
    TicketSource source,
    String subject,
    String description,
    String assignedTo,
    String resolution,
    OffsetDateTime openedAt,
    OffsetDateTime firstRespondedAt,
    OffsetDateTime assignedAt,
    OffsetDateTime resolvedAt,
    OffsetDateTime closedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}