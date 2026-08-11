package com.zhangnianshuai.servicemind.api.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

    @NotBlank
    @Size(max = 32)
    String customerNo,

    @NotEmpty
    @Size(max = 50)
    List<@Valid CreateOrderItemRequest> items
) {

    public CreateOrderRequest {
        customerNo = customerNo == null
            ? null
            : customerNo.trim();

        items = items == null
            ? null
            : List.copyOf(items);
    }
}