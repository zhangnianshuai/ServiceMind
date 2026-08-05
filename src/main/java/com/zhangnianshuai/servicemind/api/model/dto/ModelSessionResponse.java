package com.zhangnianshuai.servicemind.api.model.dto;

import java.time.Instant;
import java.util.UUID;

import com.zhangnianshuai.servicemind.domain.model.ModelProvider;

public record ModelSessionResponse(
    UUID sessionId,
    ModelProvider provider,
    String model,
    Instant expiresAt
) {
}