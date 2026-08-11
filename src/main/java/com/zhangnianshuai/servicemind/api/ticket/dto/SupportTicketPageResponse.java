package com.zhangnianshuai.servicemind.api.ticket.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record SupportTicketPageResponse(
    List<SupportTicketResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    public static SupportTicketPageResponse from(
            Page<SupportTicketResponse> source) {

        return new SupportTicketPageResponse(
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