package com.zhangnianshuai.servicemind.api.chat.dto;

public record ChatResponse(
    String text,
    String chatId,
    String credentialSource
) {
}