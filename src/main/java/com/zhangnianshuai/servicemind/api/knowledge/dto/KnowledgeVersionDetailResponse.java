package com.zhangnianshuai.servicemind.api.knowledge.dto;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeVersionStatus;

public record KnowledgeVersionDetailResponse(

    String documentNo,
    int versionNo,
    KnowledgeSourceType sourceType,
    String originalFilename,
    String mimeType,
    String contentHash,
    String rawContent,
    KnowledgeVersionStatus status,
    int chunkCount,
    String embeddingModel,
    Integer embeddingDimensions,
    String errorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime processedAt,
    OffsetDateTime publishedAt
) {
}