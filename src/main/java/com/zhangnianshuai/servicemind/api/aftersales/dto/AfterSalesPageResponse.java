package com.zhangnianshuai.servicemind.api.aftersales.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record AfterSalesPageResponse(
    List<AfterSalesResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    public static AfterSalesPageResponse from(
            Page<AfterSalesResponse> source) {

        return new AfterSalesPageResponse(
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