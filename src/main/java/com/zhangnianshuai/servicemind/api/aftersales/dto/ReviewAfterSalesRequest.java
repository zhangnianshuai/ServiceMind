package com.zhangnianshuai.servicemind.api.aftersales.dto;

import jakarta.validation.constraints.Size;

public record ReviewAfterSalesRequest(

    @Size(max = 500)
    String comment
) {
}