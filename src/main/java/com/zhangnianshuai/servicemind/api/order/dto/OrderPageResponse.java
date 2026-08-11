package com.zhangnianshuai.servicemind.api.order.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record OrderPageResponse(
    List<OrderSummaryResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    public static OrderPageResponse from(
            Page<OrderSummaryResponse> source) {

        return new OrderPageResponse(
            source.getContent(),
            source.getNumber(),
            source.getSize(),
            source.getTotalElements(),
            source.getTotalPages(),
            source.isFirst(),
            source.isLast()
        );
    }
}