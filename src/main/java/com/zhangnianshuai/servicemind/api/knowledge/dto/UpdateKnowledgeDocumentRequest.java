package com.zhangnianshuai.servicemind.api.knowledge.dto;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateKnowledgeDocumentRequest(

    @NotBlank
    @Size(max = 200)
    String title,

    @NotNull
    KnowledgeDocumentCategory category
) {
}