package com.zhangnianshuai.servicemind.api.knowledge.dto;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddKnowledgeVersionRequest(

    @NotNull
    KnowledgeSourceType sourceType,

    @Size(max = 255)
    String originalFilename,

    @Size(max = 100)
    String mimeType,

    @NotBlank
    String rawContent
) {
}