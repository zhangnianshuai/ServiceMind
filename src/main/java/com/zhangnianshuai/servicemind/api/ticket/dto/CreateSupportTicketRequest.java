package com.zhangnianshuai.servicemind.api.ticket.dto;

import com.zhangnianshuai.servicemind.domain.ticket.TicketCategory;
import com.zhangnianshuai.servicemind.domain.ticket.TicketPriority;
import com.zhangnianshuai.servicemind.domain.ticket.TicketSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSupportTicketRequest(

    @NotBlank
    @Size(max = 32)
    String customerNo,

    @Size(max = 32)
    String orderNo,

    @Size(max = 32)
    String afterSalesApplicationNo,

    @NotNull
    TicketCategory category,

    TicketPriority priority,

    @NotNull
    TicketSource source,

    @NotBlank
    @Size(max = 200)
    String subject,

    @NotBlank
    @Size(max = 2000)
    String description
) {
}