package com.zhangnianshuai.servicemind.api.aftersales.dto;

import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAfterSalesRequest(

    @NotBlank
    @Size(max = 32)
    String orderNo,

    @NotNull
    @Positive
    Long orderItemId,

    @NotNull
    AfterSalesType type,

    @NotNull
    @Positive
    Integer requestedQuantity,

    @NotBlank
    @Size(max = 200)
    String reason,

    @Size(max = 1000)
    String description
) {
}