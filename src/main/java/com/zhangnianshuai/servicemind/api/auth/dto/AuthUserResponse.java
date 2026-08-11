package com.zhangnianshuai.servicemind.api.auth.dto;

public record AuthUserResponse(
    String username,
    String displayName,
    String role
) {
}