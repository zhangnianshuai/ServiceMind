package com.zhangnianshuai.servicemind.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignTicketRequest(

    @NotBlank
    @Size(max = 100)
    String assignedTo
) {
}