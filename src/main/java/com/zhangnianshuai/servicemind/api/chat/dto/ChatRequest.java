package com.zhangnianshuai.servicemind.api.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(

    @NotBlank
    @Size(max = 4000)
    String text,

    @Size(max = 100)
    String chatId,

    UUID modelSessionId
) {
}