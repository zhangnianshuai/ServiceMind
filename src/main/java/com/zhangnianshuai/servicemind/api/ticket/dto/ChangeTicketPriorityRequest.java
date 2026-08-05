package com.zhangnianshuai.servicemind.api.ticket.dto;

import com.zhangnianshuai.servicemind.domain.ticket.TicketPriority;

import jakarta.validation.constraints.NotNull;

public record ChangeTicketPriorityRequest(

    @NotNull
    TicketPriority priority
) {
}