package com.zhangnianshuai.servicemind.api.knowledge.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;

public record KnowledgeDocumentDetailResponse(

    String documentNo,
    String title,
    KnowledgeDocumentCategory category,
    KnowledgeDocumentStatus status,
    int latestVersionNo,
    Integer activeVersionNo,
    String createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<KnowledgeVersionSummaryResponse> versions
) {
}