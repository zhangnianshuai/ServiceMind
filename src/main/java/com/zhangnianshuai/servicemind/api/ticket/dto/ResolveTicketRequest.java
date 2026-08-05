package com.zhangnianshuai.servicemind.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveTicketRequest(

    @NotBlank
    @Size(max = 2000)
    String resolution
) {
}