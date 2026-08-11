package com.zhangnianshuai.servicemind.api.auth.dto;

public record CsrfResponse(
    String headerName,
    String token
) {
}